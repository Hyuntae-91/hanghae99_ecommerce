# API 명세

## Point API
### GET Point
- 설명 : 유저의 현재 잔액을 응답하는 api
- endpoint : /v1/point
- method : GET
- header : userId

#### Response
```
{
    "userId": 1,
    "point": 100
}
```

```
{
    "code": 404,
    "message": "User Not Found"
}
```

### GET Point History
- 설명 : 유저의 포인트 충전/사용/환불 내역을 확인하는 api
- endpoint : /v1/point/history
- param : page=1, size=10, sort=createdAt (sort type: name, price, createdAt)
- method : GET
- header : userId

#### Response
```
{
    "history": [
        {
            "userId": 1,
            "point": 100,
            "type": "충전",
            "createdAt": "2025-04-03T10:20:00"
        },
        {
            "userId": 1,
            "point": 200,
            "type": "충전",
            "createdAt": "2025-04-03T10:10:00"
        },
        {
            "userId": 1,
            "point": 100,
            "type": "결제",
            "createdAt": "2025-04-03T10:00:00"
        },
        {
            "userId": 1,
            "point": 100,
            "type": "충전",
            "createdAt": "2025-04-03T09:00:00"
        }
        ...
    ]
}
```
```
{
    "code": 404,
    "message": "User Not Found"
}
```

### PUT POINT
- 설명 : 유저의 포인트 충전을 하는 api
- endpoint : /v1/point
- method : PUT
- header : userId

#### Request
```
{
    "point": 100
}
```

#### Response
```
{
    "userId": 1,
    "total_point": 500 
}
```
```
{
    "code": 400,
    "message": "Invalid point"
}
```
```
{
    "code": 404,
    "message": "User Not Found"
}
```


## Product API
### GET Product
- 설명 : 상품 상세 정보를 요청하는 API
- endpoint : /v1/products/{productId:int}
- method : GET

#### Response
```
{
    "id": 1,
    "name": "상품상세",
    "price": 1000,
    "state": 1,
    "createdAt": "2025-04-03T09:00:00",
    "options": [
    {
      "optionId": 1,
      "size": 100,
      "stock": 10
    },
    ...
  ]
}
```

```
{
    "code": 404,
    "message": "Product Not Found"
}
```

### GET Product list
- 설명 : 상품 리스트를 요청하는 API
- endpoint : /v1/products
- param : page=1, size=10, sort=createdAt (sort type: name, price, createdAt)
- method : GET

#### Response
```
GET /v1/products?page=1&size=10&sort=createdAt

Response:
{
  "products": [
    {
      "id": 1,
      "name": "상품상세",
      "price": 1000,
      "state": 1,
      "createdAt": "2025-04-03T09:00:00",
      "options": [
        {
          "optionId": 1,
          "size": 100,
          "stock": 10
        },
        ...
      ]
    },
    ...
  ]
}

```

### GET Product popular 
- 설명 : 최근 3일 이내에 인기상품 상위 5개를 요청하는 API (매 한시간마다 갱신)
- endpoint : /v1/products/bests
- method : GET

#### Response
```
{
  "products": [
    {
      "id": 1,
      "name": "상품상세",
      "price": 1000,
      "state": 1,
      "createdAt": "2025-04-03T09:00:00",
      "options": [
        {
          "optionId": 1,
          "size": 100,
          "stock": 10
        }
      ]
    },
    ...
  ]
}
```

### POST Product popular
- 설명 : 상위 product 통계를 계산하는 API
- endpoint : /v1/products/best/calculate
- method : POST

요청/응답 객체 없음

## Coupon API
### GET Coupon
- 설명 : 사용자의 보유 쿠폰을 응답하는 API
- endpoint : /v1/coupon
- method : GET
- header : userId

#### Response
```
{
    "coupons": [
        {
            "couponId": 1,
            "type": "PERCENT",
            "description": "쿠폰 내용",
            "discount": 50,
            "state": 0,
            "start_at": "2025-04-03T09:00:00",
            "end_at": ""2025-04-04T09:00:00",
            "createdAt": "2025-04-03T09:00:00"
        },
        {
            "couponId": 2,
            "type": "FIXED",
            "description": "쿠폰 내용",
            "discount": 1000,
            "state": 0,
            "start_at": "2025-04-03T09:00:00",
            "end_at": ""2025-04-04T09:00:00",
            "createdAt": "2025-04-03T09:00:00"
        },
        ...
    ]
}
```

```
{
    "code": 404,
    "message": "User Not Found"
}
```

### POST Coupon
- 설명 : 사용자에게 쿠폰을 지급하는 API
- endpoint : /v1/coupon/{couponId:int}/issue
- method : POST
- header : userId

#### Response
```
{
    "couponId": 1,
    "type": "FIXED",
    "description": "쿠폰 내용",
    "discount": 1000,
    "state": 0,
    "start_at": "2025-04-03T09:00:00",
    "end_at": ""2025-04-04T09:00:00",
    "createdAt": "2025-04-03T09:00:00"
}
```

```
{
    "code": 404,
    "message": "User Not Found"
}
```

```
{
    "code": 404,
    "message": "Coupon Not Found"
}
```


```
{
    "code": 409,
    "message": "Coupon Out of Stock"
}
```


## Order API
### Post Order Cart
- 설명 : 장바구니 추가 요청하는 API
- endpoint : /v1/order/cart
- method: POST
- header : userId

#### Request
```
{
    "productId": 1,
    "quantity": 1,
    "optionId": 1
}
```

#### Response
```
{
    "cartList": [
        {
            "productId": 1,
            "quantity": 1,
            "optionId": 1,
            "eachPrice": 1000,
            "productStock": 10,
            "size": 100
        },
        {
            "productId": 1,
            "quantity": 1,
            "optionId": 1,
            "eachPrice": 1000,
            "productStock": 10,
            "size": 100
        },
        ...
    ],
    "total_price": 1000
}
```

## Payment API
### Post Payment
- 설명 : 결제 요청하는 API
- endpoint : /v1/payment
- method : POST
- header : userId

#### Request
```
{
    "products": [
        {
            "id": 1,
            "name": "상품상세",
            "optionId": 1,
            "quantity": 10,
            "state": 1
        },
        {
            "id": 2,
            "name": "상품상세",
            "optionId": 1,
            "quantity": 10,
            "state": 1
        },
        {
            "id": 3,
            "name": "상품상세",
            "optionId": 1,
            "quantity": 10,
            "state": 1
        },
        ...
    ],
    "couponIssueId": 0 or null
}
```

### Post Payment Continue
- 설명 : 결제 요청하는 API. 주문은 생성됐지만, 결제가 취소된 경우, 이어서 다시 결제 시도 가능한 endpoint
- endpoint : /v1/payment/{orderId:int}
- method : POST
- header : userId

#### Response
```
{
    "paymentId": 1,
    "orderId": 1,
    "status": 1,
    "total_price": 10000,
    "createdAt": "2025-04-03T09:00:00"
}
```

```
{
    "code": 404,
    "message": "User Not Found"
}
```

```
{
    "code": 404,
    "message": "Order Not Found"
}
```


### DELETE Payment
- 설명 : 결제 취소 요청하는 API
- endpoint : /v1/payment/{paymentId:int}/cancel
- method : DELETE
- header : userId

#### Response
```
{
    "paymentId": 1,
    "orderId": 1,
    "status": -1
}
```

```
{
    "code": 404,
    "message": "User Not Found"
}
```

```
{
    "code": 404,
    "message": "Order Not Found"
}
```
