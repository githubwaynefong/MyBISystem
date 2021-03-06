# -*- coding:UTF-8 -*-
from com.utls.pro_env import PROJECT_CONF_DIR, HADOOP_PATH, PROJECT_LIB_DIR
from xml.etree import ElementTree
from com.utls.hive import HiveUtil
import os
import sys

# 获取漏斗模型rul正则匹配的配置信息
def resolve_conf():
    #配置文件的地址
    confFile = PROJECT_CONF_DIR + "Conversion.xml"
    #解析XML
    xmlTree = ElementTree.parse(confFile);
    #得到pras元素
    eles = xmlTree.findall('./pras')
    pras = eles[0]
    
    #用来保存漏斗的URL的集合
    urls = []
    
    for pra in pras.getchildren():
        #print pra.tag,':',pra.text
        if pra.tag == 'url':
            url = pra.text.strip()
            if url != None or url != '':
                print(url)
                urls.append(url)
                
    #检查参数有效性，否则抛出异常
    if len(urls) == 0:
        raise Exception('参数不全')
    
    return urls

def extract_data(start, end):
    #从conversion_input提取需要的数据到conversion_input
    hql = "insert into table conversion_input " \
        + "partition (dt='" + start + "-" + end + "')" \
        + "select url, uuid, session_id, csvp " \
        + "from clickstream_log " \
        + "where dt >= '" + start + "' and dt <= '" + end + "'"
    print("\n[hql:] extract data from conversion_input: \n" + hql)
    HiveUtil.execute_shell(hql)
    
def count_urls(start, end, urls, outputPath):
    #MapReduce作业的输入路径，为conversion_input表的HDFS地址
    inputPath = "/user/hive/warehouse/conversion_input/dt=" + start + "-" + end
    
    #删除上一次作业输出目录
    os.system(HADOOP_PATH + "hadoop fs -rmr " + outputPath)
    
    #将表示漏斗的正则表达式拼装成一个字段串，作为参数传给MapReduce作业
    urlstr = ""
    for i in range(len(urls)):
        if (i == len(urls) - 1):
            urlstr += urls[i]
        else:
            urlstr += urls[i] + " "
        
    #拼装成shell命令
    shell = HADOOP_PATH + "hadoop jar " + PROJECT_LIB_DIR + "conversion.jar " \
        + "com.conversion.mapreduce.Driver " + inputPath + " " + outputPath + " " + urlstr
    print("\n[hadoop cmd:] from count_urls:\n" + shell) 
    
    #执行命令
    os.system(shell)
    
# 将MR的输出路径的结果上传到Hive的转换率最终结果表中
def get_result(start, end, inputPath):
    #最终结果表的区分
    dt = start + "-" + end
    
    #删除作业成功的标志性文件
    shell = HADOOP_PATH + "hadoop fs -rm " + inputPath + "/_SUCCESS"
    print("\n[shell:] 删除作业成功的标志性文件：\n" + shell)
    os.system(shell)
    
    #删除作业的日志文件
    shell = HADOOP_PATH + "hadoop fs -rmr " + inputPath + "/_logs"
    print("\n[shell:] 删除作业的日志文件：\n" + shell)
    os.system(shell)
    
    #将临时结果加载到中间结果表
    hql = "load data inpath '" + inputPath + "' overwrite into "\
        + "table conversion_middle_result partition (dt = '" + dt + "')"
    print("\n[hql:] 将临时结果加载到中间结果表：\n" + hql)
    HiveUtil.execute_shell(hql)
    
    #对中间结果进行汇总并写入最后的结果表
    hql = "insert into table conversion_result "\
        + "partition (dt = '" + start + "-" + end + "') "\
        + "select process, count(process), count(distinct(uuid)) "\
        + "from conversion_middle_result "\
        + "where dt = '" + dt + "' group by process"
    print("\n[hql:] 对中间结果进行汇总并写入最后的结果表：\n" + hql)
    HiveUtil.execute_shell(hql)
    

if __name__ == '__main__':
    #统计时间范围开始的时间，通过命令行参数传入
    start = sys.argv[1]
    
    #统计时间范围结束的时间，通过命令行参数传入
    end = sys.argv[2]
    
    #解析配置文件
    urls = resolve_conf()
    
    #提取所需数据
    extract_data(start, end)
    
    #转换率分析的中间结果路径
    #也即：MapReduce作业的输出路径，可以任意指定
    conversion_middle_result_path = "/user/tmp/conversion"
    
    #通过MapReduce作业进行统计
    count_urls(start, end, urls, conversion_middle_result_path)
    
    #对中间结果进行汇总并得到最后结果表
    get_result(start, end, conversion_middle_result_path)
    


#*************************************************************
#resolve_conf()    #测试
#extract_data('2018-01-15', '2018-01-16')
#count_urls('2018-01-15', '2018-01-16', ['a','b'])
#get_result('2018-01-15', '2018-01-16', 'output')