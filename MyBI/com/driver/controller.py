# -*- coding:utf-8 -*-
# 注1：在'~/.bashrc'文件中加入：export PYTHONPATH=$PYTHONPATH:/home/hadoop/MyBI，作用范围当前用户
# 注2：同时在 MyBI根目录执行此文件
import sys
import os
import datetime
from xml.etree import ElementTree
from com.utls.pro_env import PROJECT_CONF_DIR


if __name__ == '__main__':
    
    reload(sys)
    
    today = datetime.date.today()
    
    yestoday = today + datetime.timedelta(-1)
    
    #获得昨天的日期
    dt = yestoday.strftime('%Y-%m-%d')
    
    #加载主配置文件
    xmlTree = ElementTree.parse(PROJECT_CONF_DIR + "workflow.xml")
    
    #获得所有task结点
    workflow = xmlTree.findall('./task')
    
    for task in workflow:
        
        #获得模块名称
        moduleName = task.text
        
        if moduleName == "exe_hql":
            #如果模块可以执行多个功能，则将task阶段的type属性一并拼装成shell
            shell = "python " + "com/cal/" + moduleName + ".py " + task.attrib.get('type') + " " + dt
            #执行shell
            os.system(shell)
            
        else:
            shell = "python " + "com/cal/" + moduleName + ".py " + dt
            print ('execute shell: ' + shell)
            os.system(shell)
            