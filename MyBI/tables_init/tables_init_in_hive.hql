----------------------------------------------------------------------
---------------------------点击流日志清洗-----------------------------
----------------------------------------------------------------------
--点击流日志（结构化）数据表
DROP TABLE IF EXISTS clickstream_log;
CREATE TABLE IF NOT EXISTS clickstream_log(
	ip_address STRING			COMMENT 'IP地址',
	uuid STRING					COMMENT '用户的唯一编号uuid/uniqueid',
	url STRING					COMMENT '用户访问的链接',
	session_id STRING			COMMENT 'Session的唯一编号',
	session_times INT			COMMENT 'Session的次数',
	area_address STRING			COMMENT '发生点击行为的地区',
	local_address STRING		COMMENT '发生点击行为的详细地址',
	browser_type STRING			COMMENT '用户浏览器信息',
	operation_sys STRING		COMMENT '用户操作系统信息',
	refer_url STRING			COMMENT '上一个浏览的网页',
	receive_time BIGINT			COMMENT '日志接收服务器接收时间',
	user_id STRING				COMMENT '用户的id',
	csvp INT					COMMENT '该点击行为在其Session中的顺序'
) COMMENT '点击流日志（结构化）数据表' 
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

----------------------------------------------------------------------
-----------------------------转化率分析-------------------------------
----------------------------------------------------------------------
--转化率维度数据提取表（从clickstream_log表中提取）
DROP TABLE IF EXISTS conversion_input;
CREATE TABLE IF NOT EXISTS conversion_input(
	url STRING					COMMENT '用户访问的链接',
	uuid STRING					COMMENT '通用唯一识别码',
	session_id STRING			COMMENT 'Session的唯一编号',
	csvp INT					COMMENT '该点击行为在其Session中的顺序'
) COMMENT '转化率维度数据提取表' 
PARTITIONED BY(dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

--转化率分析中间结果表
DROP TABLE IF EXISTS conversion_middle_result;
CREATE TABLE IF NOT EXISTS conversion_middle_result(
	session_id STRING			COMMENT 'Session的唯一编号',
	uuid STRING					COMMENT '通用唯一识别码',
	process STRING				COMMENT '漏斗模型步骤'
) COMMENT '转化率分析中间结果表'
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

--转化率分析最终统计结果表
DROP TABLE IF EXISTS conversion_result;
CREATE TABLE IF NOT EXISTS conversion_result(
	process STRING				COMMENT '漏斗模型步骤',
	process_count INT			COMMENT '统计漏斗模型各步骤的点击数',
	uuid_count INT				COMMENT '统计漏斗模型各步骤的点击人数'
) COMMENT '转化率分析最终统计结果表'
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';



----------------------------------------------------------------------
-------------------------------聚类准备-------------------------------
----------------------------------------------------------------------
--用户维度表（用于保存聚类分析前提取的维度数据）
DROP TABLE IF EXISTS user_dimensioin;
CREATE TABLE IF NOT EXISTS user_dimensioin(
	customer_id STRING			COMMENT '用户ID',
	avg_subtotal DOUBLE			COMMENT '平均金额',
	order_count DOUBLE			COMMENT '订单数',
	session_count DOUBLE		COMMENT '访问次数'
) COMMENT '用户维度表'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

--聚类输入数据表（Mahout规定只能接收空格分隔的数据）
DROP TABLE IF EXISTS cluster_input;
CREATE TABLE IF NOT EXISTS cluster_input(
	avg_subtotal DOUBLE			COMMENT '平均金额',
	order_count DOUBLE			COMMENT '订单数',
	session_count DOUBLE		COMMENT '访问次数'
) COMMENT '聚类输入数据表'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ' ';

----------------------------------------------------------------------
-------------------------------聚类结果-------------------------------
----------------------------------------------------------------------
--聚类结果保存表
DROP TABLE IF EXISTS cluster_result;
CREATE TABLE IF NOT EXISTS cluster_result(
	cluster_id INT				COMMENT '聚簇中心ID',
	avg_subtotal DOUBLE			COMMENT '平均金额',
	order_count DOUBLE			COMMENT '订单数',
	session_count DOUBLE		COMMENT '访问次数'
) COMMENT '聚类结果保存表'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

--聚类结果与用户关联表
DROP TABLE IF EXISTS cluster_result_user_lnk;
CREATE TABLE IF NOT EXISTS cluster_result_user_lnk(
	user_id String				COMMENT '用户ID',
	cluster_id INT				COMMENT '聚簇中心ID',
	avg_subtotal DOUBLE			COMMENT '平均金额',
	order_count DOUBLE			COMMENT '订单数',
	session_count DOUBLE		COMMENT '访问次数'
) COMMENT '聚类结果与用户关联表'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

----------------------------------------------------------------------
----------------------------------------------------------------------
----------------------------------------------------------------------