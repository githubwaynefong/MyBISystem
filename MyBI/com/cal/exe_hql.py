# -*- coding:UTF-8 -*-
from com.utls.pro_env import PROJECT_CONF_DIR
from xml.etree import ElementTree
import sys
from com.utls.hive import HiveUtil


#解析配置文件
def resolve_conf(job_type, dt):
    
    # 格式化dt参数，给dt: '2019-05-18' 字符串套上引号
    dt = '\"' + dt + '\"'
    
    #获得配置文件名
    confFile = PROJECT_CONF_DIR + "HiveJob.xml"
    
    #解析配置文件
    xmlTree = ElementTree.parse(confFile)
    
    #Job元素
    jobs = xmlTree.findall('./Job')
    
    #用来保存hql
    hqls = []
    
    #遍历Job的子元素，获得所需参数
    for job in jobs:
        
        #如果Job的type是需要执行的type
        if job.attrib["type"] == job_type:
            
            for hql in job.getchildren():
                #获得hql标签的值去掉两头的空字符
                hql = hql.text.strip()
                
                #检查hql有效性，无效则抛出异常
                if len(hql) == 0 or hql == "" or hql == None:
                    raise Exception("参数有误，终止运行")
                else:
                    #将时间信息替换
                    hql = hql.replace("\$dt", dt)
                hqls.append(hql)
                
    return hqls
                
#print resolve_conf("analysis", "2018-01-11")    #测试
#******解析结果*******
#['select * from test where dt = 2018-01-11', '...', '...']

#Python模块的入口：main函数
if __name__ == '__main__':
    
    
    hive_job_type = sys.argv[1]
    dt = sys.argv[2]
    
    print(hive_job_type + "模块开始执行.....\n")
        
    print("HiveJob.xml配置文件解析开始.....\n")
    
    #使用调度模块传入的两个参数，第一个为可执行的type，第二个日期
    hqls = resolve_conf(hive_job_type, dt)
    
    print("HiveJob.xml配置文件解析结束.....\n")
    
    print("hql开始执行.....\n")
    #迭代集合，执行hql语句
    for hql in hqls:
        HiveUtil.execute_shell(hql)
    
    print("hql结束执行.....\n")
    
    print(hive_job_type + "模块结束执行.....\n")
        