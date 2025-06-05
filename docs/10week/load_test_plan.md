# 부하 테스트 계획서
## 1. 테스트 개요
| 구분        | 내용                                                                 |
| --------- | ------------------------------------------------------------------ |
| **목표**    | 단일 인스턴스 기준으로 이커머스 핵심 API의 처리 한계(TPS)와 안정 임계치를 파악하여 스케일-업/아웃 전략 수립  |
| **범위**    | 상품 탐색부터 결제 완료까지의 대표 사용자 흐름(상품 목록 조회 → 장바구니 담기 → 쿠폰 조회 → 결제)을 집중 검증 |
| **성과 지표** | ① 95-percentile 응답 시간, ② 에러율, ③ 시스템 자원( CPU·메모리 ) 사용률, ④ 처리 완료 TPS |

> 실제 주문 트래픽은 “탐색 → 담기 → 할인 → 결제” 순으로 몰려 있음. 해당 경로가 안정적이면 다른 보조 API는 상대적으로 여유가 생기므로, 핵심 경로를 먼저 압축적으로 검증

## 2. 테스트 환경
### 2-1. 인프라 구성
| 영역         | 스택                                                     | 비고           |
| ---------- | ------------------------------------------------------ | ------------ |
| **애플리케이션** | Spring Boot (OpenJDK 17) <br/> CPU 2 vCore / RAM 8 GiB | Docker 컨테이너  |
| **데이터베이스** | MySQL 8.0                                              | |
| **캐시**     | Redis latest                                           | 단일 노드        |
| **메시지**    | Kafka 3.5.1                                            | 단일 브로커       |
| **모니터링**   | InfluxDB 1.8 + Grafana 11.6.1                          | k6 전용 플러그인 사용 |

### 2-2. 테스트 도구 스택
- k6: 시나리오 기반 부하 생성
- k6-InfluxDB output: 테스트 지표 저장
- Grafana: 실시간 시각화 / 이력 분석

### 2-3. 테스트 데이터셋 준비
- **UserPoint** : 100만개 데이터
- **Product** : 100만개 데이터
- **Product Options** : 250만개 데이터
- **Coupon** : 1,000 개 데이터
- **Coupon Issue** : 10,000 개 데이터

## 3. 시나리오 정의
> 하나의 사용자가 상품 목록 조회 → 장바구니 담기 → 쿠폰 조회 → 결제 를 순서대로 수행한다고 가정

| 단계 | 엔드포인트                 | 설명            |
| -- | --------------------- | ------------- |
| 1  | `GET /v1/products`    | 첫 접속 시 상품 탐색  |
| 2  | `POST /v1/order/cart` | 선택 상품 장바구니 담기 |
| 3  | `GET /v1/coupon`      | 사용 가능 쿠폰 조회   |
| 4  | `POST /v1/payment`    | 결제 요청         |

## 4. 부하 프로파일
| 구분            | 가상 사용자     | 지속 시간 | 목적                   |
| ------------- |------------| ----- | -------------------- |
| **Baseline**  | ↑ 50 VU    | 30 초  | 애플리케이션 기동 후 정상 임계 확인 |
| **Load**      | ↑ 300 VU   | 1 분   | 평시 피크 트래픽 시뮬레이션      |
| **Stress**    | ↑ 1,000 VU | 2 분   | 서비스 한계 탐색 시작         |
| **Max**       | ↑ 2,000 VU | 2 분   | 물리적 한계 노출 구간         |
| **Cool-down** | ↓ 0 VU     | 30 초  | 자원 회복 확인             |

#### 목표 지표
95 percentile 응답시간  < 500 ms
에러율                 < 5 %
성공률                 > 99 %

## 5. K6 스크립트
```javascript
import http from 'k6/http';
import { check, group, sleep, fail } from 'k6';

export const options = {
    stages: [
        { duration: "30s", target: 50 },   // 기본 성능 테스트 (50명)
        { duration: "1m", target: 300 },   // 부하 테스트 (300명)
        { duration: "2m", target: 1000 },   // 스트레스 테스트 (1000명)
        { duration: "2m", target: 2000 },   // 최대 부하 한계 테스트 (2000명)
        { duration: "30s", target: 0 }     // 부하 감소 (서버 복구 확인)
    ],
    thresholds: {
        http_req_duration: ["p(95)<500"],  // 95% 이상의 요청이 500ms 이하 유지
        http_req_failed: ["rate<0.05"],    // 실패율 5% 미만 유지
        checks: ["rate>0.99"],             // 99% 이상의 요청이 성공해야 함
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://ecommerce';

function genUserId() {
    return Math.floor(Math.random() * 1_000_000) + 1;
}

function getProductList(userId) {
    const PAGE_SIZE = 100;
    const page = Math.floor(Math.random() * 1000) + 1;

    const my_headers = { userId: String(userId) };
    const res = http.get(`${BASE_URL}/v1/products?page=${page}&size=${PAGE_SIZE}&sort=createdAt`, { headers: my_headers });

    const ok = check(res, { 'product list 200': (r) => r.status === 200 });
    if (!ok) {
        console.error(`Failed to load product list for user ${userId}. Status: ${res.status}`);
        fail('Product list load failed');
    }

    let productList;
    try {
        productList = res.json();
    } catch (e) {
        console.error('Failed to parse product list response');
        fail('Invalid JSON response');
    }

    return productList;
}

function createSelectedProducts(productList) {
    const count = Math.min(
        Math.floor(Math.random() * 3) + 1, // 1~3 랜덤
        productList.length // 상품이 적은 경우 예외 방지
    );

    const selected = [];
    const used = new Set();

    while (selected.length < count) {
        const idx = Math.floor(Math.random() * productList.length);
        if (used.has(idx)) continue;
        used.add(idx);

        const p = productList[idx];
        selected.push({
            productId: p.id,
            optionId: p.options?.[0]?.optionId ?? 1,
            quantity: selected.length === 0 ? 2 : 1,
        });
    }
    return selected;
}

function addCart(product, userId) {
    const my_headers = { userId: String(userId), 'Content-Type': 'application/json' };
    const payload = JSON.stringify({
        productId: product.productId,
        optionId: product.optionId,
        quantity: product.quantity,
    });

    const res = http.post(`${BASE_URL}/v1/order/cart`, payload, { headers: my_headers });
    const ok = check(res, { 'add cart 200': (r) => r.status === 200 });
    if (!ok) {
        console.error(`Failed to add card for user ${userId}. Status: ${res.status}`);
        fail('add card failed');
    }
    return res
}

function getCoupon(userId) {
    const my_headers = { userId: String(userId) };

    const res = http.get(`${BASE_URL}/v1/coupon`, { headers: my_headers });
    const ok = check(res, { 'get coupon 200': (r) => r.status === 200 });
    if (!ok) {
        console.error(`Failed to get Coupon for user ${userId}. Status: ${res.status}`);
        fail('get coupon failed');
    }
    return res
}

function pay(userId, products, coupon) {
    const my_headers = { userId: String(userId), 'Content-Type': 'application/json' };
    const payload = JSON.stringify({
        products: products.map((p) => ({
            id: p.id,
            name: p.name,
            itemId: p.optionId,
            optionId: p.optionId,
            quantity: p.quantity,
        })),
        couponId: coupon?.id ?? null,
        couponIssueId: coupon?.issueId ?? null,
    });

    const res = http.post(`${BASE_URL}/v1/payment`, payload, { headers: my_headers });
    check(res, { 'payment 200': (r) => r.status === 200 }) ||
    fail(`payment status=${res.status}`);
}

export default function () {
    const userId = genUserId();

    group('Scenario Test', function () {
        const productList = group('Get Product List', function () {
            return getProductList(userId);
        });

        if (!productList || productList.length === 0) {
            console.warn('No product data available.');
            return;
        }

        sleep(1);

        const selectedProducts = createSelectedProducts(productList);
        group('Add Cart', function () {
            for (const product of selectedProducts) {
                addCart(product, userId);
                sleep(1);
            }
        });

        sleep(1);

        const coupon = group('Get Coupon', function () {
            return getCoupon(userId);
        });

        sleep(1);

        group('payment', () => pay(userId, selectedProducts, coupon));
    });
}
```
