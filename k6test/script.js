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
