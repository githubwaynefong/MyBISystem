# -*- coding:UTF-8 -*-
from __builtin__ import staticmethod
import commands


class HiveUtil(object):
    
    def __init__(self):
        pass
    
    @staticmethod
    def execute_shell(hql):
        
        #将hql语句进行字符串转义
        hql = hql.replace("\"","'")
        # hive 查询语句格式： hive -S -e "select * from orders where dt = '2019-05-18'"
        # 查询语句外部用双引号，内部时间用单引号，否则执行错误
        hive_cmd = "hive -e \"" + hql + "\""
        
        #执行查询，并取得执行的状态和输出
        status, output = commands.getstatusoutput(hive_cmd)
        
        if status != 0:
            print "HQL execute failed :\n"
            print hive_cmd
            print output
            return None
        else:
            print "HQL execute success :\n"
            print hive_cmd
            print output
            
        output = str(output).split("\n")
        
        return output
