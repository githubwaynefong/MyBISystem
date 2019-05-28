# 聚类输入数据表（Mahout规定只能接收空格分隔的数据）
DROP TABLE IF EXISTS cluster_input;
CREATE TABLE IF NOT EXISTS cluster_input(
	avg_subtotal DOUBLE			COMMENT '平均金额',
	order_count DOUBLE			COMMENT '订单数',
	session_count DOUBLE		COMMENT '访问次数'
) COMMENT '聚类输入数据表';

insert into cluster_input (avg_subtotal, order_count, session_count) values
(100, 5, 20),
(1000, 6, 2),
(301, 10, 1),
(578, 9, 40),
(154, 3, 8),
(1002, 1, 3),
(899, 3, 10),
(300, 36, 6),
(290, 20, 5),
(2356, 12, 12);

select * from cluster_input;

# 对各特征值列进行Z分数归一
select (avg_subtotal - avg_avg_subtotal)/std_avg_subtotal avg_subtotal
	  ,(order_count - avg_order_count)/std_order_count order_count
      ,(session_count - avg_session_count)/std_session_count session_count
from cluster_input t0
join (
	select avg(avg_subtotal) avg_avg_subtotal,avg(order_count) avg_order_count,avg(session_count) avg_session_count
    from cluster_input
) t1 on 1 = 1
join (
	select std(avg_subtotal) std_avg_subtotal,std(order_count) std_order_count,std(session_count) std_session_count
    from cluster_input
) t2 on 1 = 1;

# 相关系数矩阵（归一化不影响相关性计算）
select round((avg(avg_subtotal*avg_subtotal)-avg(avg_subtotal)*avg(avg_subtotal)) / (std(avg_subtotal)*std(avg_subtotal)), 3) p11
	  ,round((avg(avg_subtotal*order_count)-avg(avg_subtotal)*avg(order_count)) / (std(avg_subtotal)*std(order_count)), 3) p12
      ,round((avg(avg_subtotal*session_count)-avg(avg_subtotal)*avg(session_count)) / (std(avg_subtotal)*std(session_count)), 3) p13
      ,round((avg(order_count*avg_subtotal)-avg(order_count)*avg(avg_subtotal)) / (std(order_count)*std(avg_subtotal)), 3) p21
	  ,round((avg(order_count*order_count)-avg(order_count)*avg(order_count)) / (std(order_count)*std(order_count)), 3) p22
      ,round((avg(order_count*session_count)-avg(order_count)*avg(session_count)) / (std(order_count)*std(session_count)), 3) p23
      ,round((avg(session_count*avg_subtotal)-avg(session_count)*avg(avg_subtotal)) / (std(session_count)*std(avg_subtotal)), 3) p31
	  ,round((avg(session_count*order_count)-avg(session_count)*avg(order_count)) / (std(session_count)*std(order_count)), 3) p32
      ,round((avg(session_count*session_count)-avg(session_count)*avg(session_count)) / (std(session_count)*std(session_count)), 3) p33
from cluster_input;