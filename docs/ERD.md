# ERD 설계

## ER 다이어그램
![ERD](./image/ERD.png)
```
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
        VARCHAR type
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    COUPON||--o{COUPON_ISSUE: has
    COUPON{
        INT id PK
        VARCHAR type
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
