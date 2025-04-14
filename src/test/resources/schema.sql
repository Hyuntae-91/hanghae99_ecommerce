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
    updated_at timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint point_history_user_point_id_fk
        foreign key (user_id) references user_point (id)
)
    comment '유저 포인트 히스토리';

