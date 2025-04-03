# 이커머스 프로젝트
## 프로젝트 개요
본 프로젝트는 Java와 Spring Boot 기반으로 구현된 간단한 이커머스 시스템입니다.  
사용자는 포인트를 충전하여 상품을 구매할 수 있으며, 쿠폰을 적용하여 할인된 가격으로 결제할 수 있습니다.  
관리자는 인기 상품 통계를 주기적으로 갱신할 수 있고, 전체 상품 및 주문 데이터를 관리할 수 있습니다.

해당 시스템은 다음과 같은 핵심 기능을 포함합니다:
- ✅ 유저 포인트 충전 및 사용 이력 관리
- ✅ 상품 목록 조회 및 상세 정보 확인
- ✅ 쿠폰 발급 및 적용
- ✅ 주문 생성 및 취소
- ✅ 포인트 기반 결제 및 결제 취소 처리
- ✅ 인기 상품 통계 갱신
- ✅ Swagger 기반 API 문서 제공

### Prerequisites

- Java 17 이상
- Gradle (또는 `./gradlew` 사용)
- MySQL 8.0 이상 설치 및 실행

#### Running Docker Containers
```bash
docker-compose up -d
```


# 📖 설계 문서

## 📋 요구사항 분석
👉 [요구사항 분석 보기](./docs/요구사항분석.md)

## 🔄 시퀀스 다이어그램
👉 [시퀀스 다이어그램 보기](./docs/시퀀스다이어그램.md)

## 🗂️ ERD
👉 [ERD 보기](./docs/ERD.md)

## 📘 API 문서 보기
👉 [API 명세 보기](./docs/API명세.md) 

👉 [SwaggerHub에서 보기](https://app.swaggerhub.com/apis-docs/hanghae99-213/hanghae99_ecommerce/v1)

## 📝 회고
👉 [회고 보기](./docs/회고.md)
