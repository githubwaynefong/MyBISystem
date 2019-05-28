# -*- coding:UTF-8 -*-
from com.utls.sqoop import SqoopUtil
import sys

'''
Sqoop导出类
'''
class Sqoop_Export(SqoopUtil):
    
    # 需要注入到Sqoop语句中的条件
    dt = None
    
    '''
    Sqoop_Export operation

    '''
    def __init__(self, dt):
        # 格式化dt参数，给dt: '2019-05-18' 字符串套上引号
        self.dt = '\"' + dt + '\"'
        # 设置Sqoop 操作类型为导入
        self.sqoop_type = self.EXPORT
    
    # 实现自定义键值对处理
    def key_value_resolve_customize(self, key, value):

        #将导出路径的分区替换为传入的时间
        if (key == "export-dir"):
            value = value.replace("\$dt", self.dt)
        
        #将导出分区替换为传入的时间
        if (key == "hive-partition-value"):
            value = value.replace("\$dt", self.dt)
        
        return key, value


#Python模块的入口：main函数
#
# @param: dt 字符串日期
#
# 测试方式：MyBI> python com/cal/import.py '2019-05-18'
#
if __name__ == '__main__':
    
    #调度模块将昨天的时间传入
    # python export.py dt 第一个参数argv[0]为export.py 第二个参数argv[1]为dt
    dt = sys.argv[1]
    print("dt:" + dt + "\n")
    
    Sqoop_Export(dt).driver()