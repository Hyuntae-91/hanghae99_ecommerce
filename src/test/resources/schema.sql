create table user_point
(
    id         int auto_increment
        primary key,
    point      bigint    default 0                 not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table point_history
(
    id         int auto_increment
        primary key,
    user_id    int                                 not null,
    point      bigint                              not null,
    type       enum ('CHARGE', 'USE', 'REFUND')    not null comment '유저 포인트 타입 (충전, 사용, 환불)',
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
    comment '유저 포인트 히스토리';


CREATE TABLE product
(
    id int auto_increment primary key,
    name VARCHAR(20) not null,
    price bigint not null,
    state tinyint not null comment '-1: 삭제, 1: 판매중, 2: 품절, 3: 숨김 등',
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '상품 정보';

CREATE TABLE `order`
(
    id int auto_increment primary key,
    user_id int not null,
    coupon_issue_id int null,
    total_price bigint null,
    state tinyint not null comment '-1: 주문 취소, 0: 주문 대기, 1: 주문 완료',
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '주문 정보';

CREATE TABLE order_item
(
    id int auto_increment primary key,
    order_id int null,
    user_id int not null,
    order_option_id int not null,
    product_id int not null,
    quantity int not null,
    each_price bigint not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '주문 상품(장바구니) 정보';

CREATE TABLE order_option
(
    id int auto_increment primary key,
    product_id int not null,
    size int not null,
    stock_quantity int not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '상품 옵션 정보';

CREATE TABLE payment
(
    id int auto_increment primary key,
    order_id int not null,
    total_price bigint not null,
    state tinyint not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '결제 정보';

CREATE TABLE coupon
(
    id int auto_increment primary key,
    `type` enum ('PERCENT', 'FIXED') not null comment '쿠폰 유형',
    description VARCHAR(100),
    discount int not null,
    quantity int default 0 not null,
    issued int not null,
    expiration_days int not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '쿠폰 정보';

CREATE TABLE coupon_issue
(
    id int auto_increment primary key,
    user_id int not null,
    coupon_id int not null,
    state tinyint not null comment '-1: 만료, 0: 미사용, 1: 사용',
    start_at timestamp,
    end_at timestamp,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
)
comment '쿠폰 발급 정보';