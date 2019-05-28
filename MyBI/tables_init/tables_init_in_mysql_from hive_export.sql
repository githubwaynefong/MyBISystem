# 订单信息表
drop table if exists orders_export;
create table if not exists orders_export (
	id bigint not null auto_increment primary key comment '主键ID',
    customer_id bigint not null comment '客户ID',
    order_status varchar(2) comment '订单状态：00|新订单未付款、01|已付款配送中、02|已收货、99|订单取消',
    order_date timestamp comment '订单时间',
    shipping_handling varchar(20) comment '发货快递',
    ship_to_name varchar(30) comment '收货人姓名',
    ship_to_address_id bigint comment '收货人地址',
    subtotal double	comment '小计（元）',
    tax double comment '税（元）',
    creditcard_type varchar(2) comment '信用卡类型：00、01',
    creditcard_number varchar(20) comment '信用卡号码',
    expiration_date date comment '信用卡到期日期',
    name_on_card varchar(30) comment '持卡人签名',
    approval_code varchar(20) comment '授权代码',
    modify_date timestamp comment '订单修改时间'
) comment '订单信息表';