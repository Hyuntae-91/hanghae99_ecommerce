# ERD 설계

## ER 다이어그램
```mermaid
erDiagram
    USER||--o{COUPON_ISSUE: has
    USER{
        INT id PK
        BIGINT point
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    USER||--o{POINT_HISTORY: has
    POINT_HISTORY{
        INT id PK
        INT user_id FK
        BIGINT point
        ENUM type
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    COUPON||--o{COUPON_ISSUE: has
    COUPON{
        INT id PK
        ENUM type
        VARCHAR description
        INT discount
        INT quantity
        INT issued
        INT expiration_days
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    COUPON_ISSUE{
        INT id PK
        INT user_id FK
        INT coupon_id FK
        TINYINT state
        TIMESTAMP start_at
        TIMESTAMP end_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    PRODUCT||--||PRODUCT_STOCK: is
    PRODUCT{
        INT id PK
        VARCHAR name
        INT price
        TINYINT state
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    PRODUCT_STOCK{
        INT id PK
        INT product_id FK
        INT stock
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    USER||--o{ORDER: has
    COUPON_ISSUE||--||ORDER: has
    PRODUCT||--o{ORDER: has
    ORDER{
        INT id PK
        INT user_id FK
        INT coupon_issue_id FK
        INT product_id FK
        INT quantity
        BIGINT total_price
        TINYINT state
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    ORDER||--o{PAYMENT: has
    PAYMENT{
        INT id PK
        INT order_id FK
        TINYINT state
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
```

## 설계 내용

### USER
| 컬럼 이름     | 타입      | 제약 조건                     | 설명    |
|--------------|-----------|---------------------------|-------|
| id           | INT       | PK (Primary Key)          | 기본 키  |
| point        | BIGINT    | NOT NULL                  | 포인트 값 |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP                          | 수정 일시 |


### POINT HISTORY
| 컬럼 이름     | 타입      | 제약 조건                                | 설명                        |
|--------------|-----------|--------------------------------------|-----------------------------|
| id           | INT       | PK                                   | 기본 키                      |
| user_id      | INT       | FK                                   | 사용자 ID (외래 키)          |
| point        | BIGINT    | NOT NULL                             | 포인트 값                    |
| type         | ENUM      | NOT NULL (`CHARGE`, `USE`, `REFUND`) | 포인트 사용 유형 (열거형)     |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP            | 생성 일시                    |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP          | 수정 일시                    |


### COUPON
| 컬럼 이름           | 타입        | 제약 조건                       | 설명                |
|-----------------|-----------|-----------------------------|-------------------|
| id              | INT       | PK                          | 기본 키              |
| type            | ENUM      | NOT NULL (`PERCENT`, `FIXED`) | 쿠폰 유형 (열거형)       |
| description     | VARCHAR   |                             | 쿠폰 설명             |
| discount        | INT       | NOT NULL                    | 할인 금액 또는 할인율      |
| quantity        | INT       | NOT NULL                    | 발행 가능 수량          |
| issued          | INT       | NOT NULL DEFAULT 0          | 현재까지 발행된 수량       |
| expiration_days | INT       | NOT NULL                    | 발행 후 유효 기간 (일 단위) |
| created_at      | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP   | 생성 일시             |
| updated_at      | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시             |


### COUPON ISSUE
| 컬럼 이름     | 타입      | 제약 조건                     | 설명                         |
|--------------|-----------|-------------------------------|----------------------------|
| id           | INT       | PK                             | 기본 키                       |
| user_id      | INT       | FK                             | 사용자 ID (외래 키)              |
| coupon_id    | INT       | FK                             | 쿠폰 ID (외래 키)               |
| state        | TINYINT   | NOT NULL                      | 쿠폰 상태 (-1: 만료, 0: 미사용, 1: 사용) |
| start_at     | TIMESTAMP |                               | 쿠폰 사용 시작 시간                |
| end_at       | TIMESTAMP |                               | 쿠폰 만료 시간                   |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP     | 생성 일시                      |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP   | 수정 일시                      |


### PRODUCT
| 컬럼 이름     | 타입      | 제약 조건                     | 설명                                     |
|--------------|-----------|-------------------------------|----------------------------------------|
| id           | INT       | PK                             | 기본 키                                   |
| name         | VARCHAR   | NOT NULL                      | 상품 이름                                  |
| price        | INT       | NOT NULL                      | 상품 가격 (단위: 원)                          |
| state        | TINYINT   | NOT NULL                      | 상품 상태 (-1: 삭제, 1: 판매중, 2: 품절, 3: 숨김 등) |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP     | 생성 일시                                  |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP   | 수정 일시                                  |


### PRODUCT STOCK
| 컬럼 이름     | 타입      | 제약 조건                      | 설명                         |
|--------------|-----------|----------------------------|------------------------------|
| id           | INT       | PK                         | 기본 키                      |
| product_id   | INT       | FK                         | 연결된 상품 ID (외래 키)      |
| stock        | INT       | NOT NULL                   | 재고 수량                    |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP  | 생성 일시                    |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시                    |


### ORDER
| 컬럼 이름         | 타입      | 제약 조건                       | 설명                                    |
|------------------|-----------|-----------------------------|---------------------------------------|
| id               | INT       | PK                          | 기본 키                                  |
| user_id          | INT       | FK                          | 사용자 ID (외래 키)                         |
| coupon_issue_id  | INT       | FK, NULL                    | 발급된 쿠폰 ID (외래 키), 선택 가능               |
| product_id       | INT       | FK                          | 상품 ID (외래 키)                          |
| quantity         | INT       | NOT NULL                    | 구매 수량                                 |
| total_price      | BIGINT    | NOT NULL                    | 총 결제 금액 (할인 반영 후)                     |
| state            | TINYINT   | NOT NULL                    | 주문 상태 (-1: 주문 취소, 0: 주문 대기, 1: 주문 완료) |
| created_at       | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP   | 생성 일시                                 |
| updated_at       | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시                                 |


### PAYMENT
| 컬럼 이름     | 타입      | 제약 조건                    | 설명                                    |
|--------------|-----------|------------------------------|---------------------------------------|
| id           | INT       | PK                            | 기본 키                                  |
| order_id     | INT       | FK                  | 주문 ID (외래 키)                          |
| state        | TINYINT   | NOT NULL                     | 상태 코드 (-1: 결제 취소, 0: 결제 대기, 1: 결제 완료) |
| created_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP    | 생성 일시                                 |
| updated_at   | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP  | 수정 일시                                 |
