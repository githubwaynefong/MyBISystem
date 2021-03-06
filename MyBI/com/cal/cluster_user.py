# -*- coding:UTF-8 -*-
from com.utls.hive import HiveUtil
from com.utls.pro_env import HADOOP_PATH, PROJECT_LIB_DIR, PROJECT_TMP_DIR
import os
import sys

def prepare_normaliz(start_time, end_time):
    
    ##聚类数据准备
    
    #1.提取用户维度数据，保存至'user_dimension'表中
    hql = "insert overwrite table user_dimension "\
        + "select t1.customer_id, t1.avg, t2.order_count, t3.session_count "\
        + "from ("\
        + " select customer_id, avg(subtotal) avg from booksys.orders "\
        + " where dt >= '" + start_time + "' and dt <= '" + end_time + "'"\
        + " group by customer_id"\
        + ") t1 "\
        + "join ("\
        + " select customer_id, count(id) order_count from booksys.orders "\
        + " where dt >= '" + start_time + "' and dt <= "'' + end_time + "'"\
        + " group by customer_id"\
        + ") t2 on t1.customer_id = t2.customer_id "\
        + "join ("\
        + " select user_id, count(session_id) session_count from clickstream_log "\
        + " where dt >= '" + start_time + "' and dt <= '" + end_time + "'"\
        + " group by user_id"\
        + ") t3 on t1.customer_id = t3.user_id"
    #HiveUtil.execute_shell(hql)
    
    #2.准备Mahout输入数据格式（以空格分隔数据），保存至'cluster_input'表中
    hql = "insert overwrite table cluster_input "\
        + "select avg_subtotal, order_count, session_count from user_dimension"
    #HiveUtil.execute_shell(hql)
    
    #3.归一化维度数据（z分数归一）更新至'cluster_input'表中
    hql = "insert overwrite table cluster_input "\
        + "select (avg_subtotal - avg_avg_subtotal)/std_avg_subtotal "\
        + " ,(order_count - avg_order_count)/std_order_count "\
        + " ,(session_count - avg_session_count)/std_session_count "\
        + "from cluster_input "\
        + "join ("\
        + " select std(avg_subtotal) std_avg_subtotal, std(order_count) std_order_count"\
        + " ,std(session_count) std_session_count from cluster_input"\
        + ") t1 on 1 = 1 "\
        + "join ("\
        + " select avg(avg_subtotal) avg_avg_subtotal, avg(order_count) avg_order_count"\
        + " ,avg(session_count) avg_session_count from cluster_input"\
        + ") t2 on 1 = 1 "
    HiveUtil.execute_shell(hql)
     
     
def cluster_output():
    ##聚类结果输出
    
    clusterOutputPath = "/user/hadoop/cluster_output"    # 聚类输出路径
    t1 = '100'  # Canopy 外圈距离阈值
    t2 = '10'   # Canopy 内圈距离阈值
    convergenceDelta = '0.5'    # KMeans 收敛阈值
    maxIterations = '10'    # KMeans 最大迭代次数
    
    #执行聚类
    shell = HADOOP_PATH + "hadoop jar " + PROJECT_LIB_DIR + "usercluster.jar"\
        + " com.mahout.UserCluster " + clusterOutputPath + " " \
        + "/user/hive/warehouse/cluster_input " + t1 + " " + t2 + " "\
        + convergenceDelta + " " + maxIterations
    os.system(shell)
    
    #解析聚类结果文件并输出至本地
    resultPath = PROJECT_TMP_DIR + "result"
    shell = HADOOP_PATH + "hadoop jar " + PROJECT_LIB_DIR + "clusterout.jar"\
        + " com.out.ClusterOutput " + clusterOutputPath + " " + resultPath
    os.system(shell)
    
    #将本地的结果文件加载Hive中
    hql = "load data local inpath '" + resultPath + "' overwrite into table cluster_result"
    HiveUtil.execute_shell(hql)
    
    
def get_finalresult():
    ##将聚类结果与用户信息关联
    
    hql = "insert overwrite table cluster_result_user_lnk "\
        + "select t2.customer_id, t1.* "\
        + "from ("\
        + " select cluster_id, avg_subtotal, order_count, session_count "\
        + " from cluster_result "\
        + " group by cluster_id, avg_subtotal, order_count, session_count "\
        + ") t1 "\
        + "join ("\
        + " select customer_id, (avg_subtotal - avg_avg_subtotal)/std_avg_subtotal avg_subtotal"\
        + " , (order_count - avg_order_count)/std_order_count order_count"\
        + " , (session_count - avg_session_count)/std_session_count session_count"\
        + " from user_dimension"\
        + " join ("\
        + "  select std(avg_subtotal) std_avg_subtotal, std(order_count) std_order_count"\
        + "  , std(session_count) std_session_count"\
        + "  from user_dimension"\
        + " ) t1 on 1 = 1 "\
        + " join ("\
        + "  select avg(avg_subtotal) avg_avg_subtotal, avg(order_count) avg_order_count"\
        + "  , avg(session_count) avg_session_count"\
        + "  from user_dimension"\
        + " ) t2 on 1 = 1 "\
        + ") t2 on t1.avg_subtotal = t2.avg_subtotal"\
        + " and t1.order_count = t2.order_count"\
        + " and t1.session_count = t2.session_count"
        ## group by 语句 用于去重。（存在两个用户的维度数据相同的情况）
    HiveUtil.execute_shell(hql)
    
    
if __name__ == '__main__':
    
    start_time = sys.argv[1]
    end_time = sys.argv[2]
    
    #准备数据并做数据归一化
    prepare_normaliz(start_time, end_time)
    
    #聚类并输出
    cluster_output()
    
    #得到聚类结果
    #get_finalresult()
    
    