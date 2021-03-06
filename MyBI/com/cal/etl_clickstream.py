# -*- coding:UTF-8 -*-
import sys
import os
from com.utls.pro_env import PROJECT_LIB_DIR
from com.utls.hive import HiveUtil

if __name__ == '__main__':
    
    dateStr = sys.argv[1]
    
    #由日志服务器上传至HDFS的目录下，按照时间进行存储
    inputPath = "/tmp/apache_log/" + dateStr
    
    #输出目录为Clickstream_log表的分区目录下
    outputPath = "/user/hive/warehouse/clickstream_log/dt=" + dateStr
    
    shell = "hadoop jar " + PROJECT_LIB_DIR + \
        "clickstream_etl.jar com.etl.mapreduce.Driver " + \
        inputPath + " " + outputPath 
    os.system(shell)
    
    # 删除分区目录下的"_SUCCESS"文件
    shell = "hadoop fs -rm " + outputPath + "/_SUCCESS"
    os.system(shell)
    
    # 并将分区目录下所有文件上传到Hive表中（实际上数据位置没有变，完成了注册）
    hql = "load data inpath '" + outputPath + "/' " + "overwrite into \
        table clickstream_log partition(dt='" + dateStr + "')"
    HiveUtil.execute_shell(hql)    
    