use booksys;
# 客户信息表
drop table if exists customer;
create table if not exists customer(
	id bigint not null auto_increment primary key comment '主键ID',
    name varchar(30) comment '客户姓名',
	age int comment '年龄',
    phone_number varchar(15) comment '联系电话',
    descriptor varchar(100) comment '描述'
) comment '客户信息表'; 
insert into customer (id, name, age, phone_number, descriptor) values
(1, '史强', 248, '18902134501', '警察，冬眠者，罗辑的保护者'),
(2, '汪淼', 46, '18902134502', '纳米材料科学家，用纳米线切割“审判日”战舰的人'),
(3, '叶文洁', 73, '18902134503', '向三体发送信号的天体物理学家，人类毁灭元凶'),
(4, '伊文斯', 68, '18902134504', '降临派领袖，物种共产主义者'),
(5, '罗辑', 236, '18902134505', '洞悉宇宙社会法则的地球人，面壁者，冬眠者，执剑人，最后守护地球墓碑的人'),
(6, '章北海', 238, '18902134506', '坚定的逃亡主义者，冬眠者，太空军'),
(7, '庄颜', 23, '18902134507', '罗辑塑造的梦中情人'),
(8, '云天明', 32, '18902134508', '送程心一颗星星的人，大脑流浪太空的人'),
(9, '程心', 231, '18902134509', '云天明暗恋的人，新一代执剑人，圣母'),
(10, '智子', 2, '18902134510', 'AI机器人，三体驻地球大使');

# 地址信息表
drop table if exists address;
create table if not exists address(
	id bigint not null auto_increment primary key comment '主键ID',
    customer_id bigint not null comment '客户ID',
    address varchar(100) comment '详细地址',
    country varchar(30) comment '国家'
) comment '地址信息表';
insert into address (id, customer_id, address, country) values 
(1, 1, '警察局', '中国'),
(2, 2, '科学路', '中国'),
(3, 3, '红岸基地', '中国'),
(4, 4, '审判日号', '巴拿马'),
(5, 5, '有山有水无人地', '未知'),
(6, 6, '军舰', '中国'),
(7, 7, '小说', '中国'),
(8, 8, '医院', '中国'),
(9, 9, '联合国', '美国'),
(10, 10, '树叶', '地球');


# 物品类型
drop table if exists item_type;
create table if not exists item_type(
	id bigint not null auto_increment primary key comment '主键ID',
    name varchar(30) comment '类型名称',
    description varchar(250) comment '类型描述'
) comment '物品类型';
insert into item_type (id, name, description) values
(1, '技术', '技术书籍'),
(2, '小说', '小说书籍'),
(3, '地理', '地理书籍'),
(4, '历史', '历史书籍');

# 物品信息表
drop table if exists items;
create table if not exists items (
	id bigint not null auto_increment primary key comment '主键ID',
    name varchar(30) comment '物品名称',
    item_type_id bigint comment '物品类型ID',
    description varchar(250) comment '描述',
    unitcost double comment '单位成本（元）',
    unitprice double comment '单价（元）',
    modify_date timestamp not null default '0000-00-00 00:00:00' comment '修改时间'
) comment '物品信息表';
insert into items (id, name, item_type_id, description, unitcost, unitprice, modify_date) values
(1, 'Hadoop海量数据处理', 1, '海量数据分布式存储平台', 49.00, 59.00, timestamp('2019-05-16 13:13:13')),
(2, 'HBase实战', 1, '分布式列族存储数据库', 40.00, 53.00, timestamp('2019-05-16 13:13:13')),
(3, 'Hive编程指南', 1, 'HDFS文件查询工具', 70.00, 89.00, timestamp('2019-05-16 13:13:13')),
(4, 'Sqoop工具', 1, '数据迁移工具', 20.00, 35.00, timestamp('2019-05-16 13:13:13')),
(5, 'MapReduce编程', 1, '大数据处理计算框架', 24.00, 42.00, timestamp('2019-05-16 13:13:13')),
(6, 'Spark编程', 1, '大数据处理计算框架', 30.00, 46.00, timestamp('2019-05-16 13:13:13')),
(7, '三体', 2, '地球往事第一部', 30.00, 41.00, timestamp('2019-05-16 13:13:13')),
(8, '黑暗森林', 2, '地球往事第二部', 31.00, 42.00, timestamp('2019-05-16 13:13:13')),
(9, '死神永生', 2, '地球往事第三部', 32.00, 43.00, timestamp('2019-05-16 13:13:13')),
(10, '丝绸之路', 4, '一部全新世界史', 80.00, 128.00, timestamp('2019-05-16 13:13:13'));

# 物品分类表
drop table if exists cateries;
create table if not exists cateries(
	id bigint not null auto_increment primary key comment '主键ID',
    parent_id bigint comment '父ID',
    name varchar(30) comment '分类名称',
    description varchar(250) comment '描述',
    is_leaf varchar(1) comment '是否叶子节点：0|否、1|是'
) comment '物品分类表';
insert into cateries (id, parent_id, name, description, is_leaf) values 
(1, null, '纸质类', '', '0'),
(2, 1, '书籍', '书籍类', '1'),
(3, 1, '报刊', '报刊类', '1'),
(4, 1, '杂志', '杂志类', '1');

# 物品-分类关联表
drop table if exists item_catery_lnk;
create table if not exists item_catery_lnk(
	item_id bigint not null comment '物品ID',
    catery_id bigint not null comment '分类ID',
    modify_date timestamp not null default '0000-00-00 00:00:00' comment '修改时间'
) comment '物品-分类关联表';
insert into item_catery_lnk (item_id, catery_id, modify_date) values
(1, 2, timestamp('2019-05-16 13:13:13')),
(2, 2, timestamp('2019-05-16 13:13:13')),
(3, 2, timestamp('2019-05-16 13:13:13')),
(4, 2, timestamp('2019-05-16 13:13:13')),
(5, 2, timestamp('2019-05-16 13:13:13')),
(6, 2, timestamp('2019-05-16 13:13:13')),
(7, 2, timestamp('2019-05-16 13:13:13')),
(8, 2, timestamp('2019-05-16 13:13:13')),
(9, 2, timestamp('2019-05-16 13:13:13')),
(10, 2, timestamp('2019-05-16 13:13:13'));

# 订单信息表
drop table if exists orders;
create table if not exists orders (
	id bigint not null auto_increment primary key comment '主键ID',
    customer_id bigint not null comment '客户ID',
    order_status varchar(2) comment '订单状态：00|新订单未付款、01|已付款配送中、02|已收货、99|订单取消',
    order_date timestamp not null default '0000-00-00 00:00:00' comment '订单时间',
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
    modify_date timestamp not null default '0000-00-00 00:00:00' comment '订单修改时间'
) comment '订单信息表';
insert into orders (id, customer_id, order_status, order_date, shipping_handling, ship_to_name, ship_to_address_id
, subtotal, tax, creditcard_type, creditcard_number, expiration_date, name_on_card, approval_code, modify_date) values
(1, 1, '00', timestamp('2019-05-16 13:13:13'), '顺丰', '史强', 1, 243.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13')),
(2, 2, '01', timestamp('2019-05-16 13:13:13'), '申通', '汪淼', 2, 243.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13')),
(3, 3, '02', timestamp('2019-05-16 13:13:13'), '菜鸟', '叶文洁', 3, 204.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13')),
(4, 4, '99', timestamp('2019-05-16 13:13:13'), '韵达', '伊文斯', 4, 118.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13')),
(5, 5, '00', timestamp('2019-05-16 13:13:13'), '中通', '罗辑', 5, 118.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13')),
(6, 6, '02', timestamp('2019-05-16 13:13:13'), '苏宁', '章北海', 6, 118.0, 0, '','',null,'','',timestamp('2019-05-16 13:13:13'));

# 订单明细表
drop table if exists order_items;
create table if not exists order_items (
	order_id bigint not null comment '订单ID',
    item_id bigint not null comment '物品ID',
    unitprice double comment '单价（元）',
    quantity int comment '数量',
    subtotal double comment '小计（元）',
    modify_date timestamp not null default '0000-00-00 00:00:00' comment '修改时间'
) comment '订单明细信息'; 
insert into order_items (order_id, item_id, unitprice, quantity, subtotal, modify_date) values
(1, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13')),
(1, 2, 53.00, 1, 53.0, timestamp('2019-05-16 13:13:13')),
(1, 5, 24.00, 3, 72.0, timestamp('2019-05-16 13:13:13')),
(2, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13')),
(2, 2, 53.00, 1, 53.0, timestamp('2019-05-16 13:13:13')),
(2, 3, 24.00, 3, 72.0, timestamp('2019-05-16 13:13:13')),
(3, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13')),
(3, 9, 43.00, 2, 86.0, timestamp('2019-05-16 13:13:13')),
(4, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13')),
(5, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13')),
(6, 1, 59.00, 2, 118.0, timestamp('2019-05-16 13:13:13'));
