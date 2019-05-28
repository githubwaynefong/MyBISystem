# -*- coding:UTF-8 -*-
import sys
from com.utls.sqoop import SqoopUtil

'''
Sqoop导入类
'''
class Sqoop_Import(SqoopUtil):
    
    # 需要注入到Sqoop语句中的条件
    dt = None
    import_condition = None
    
    '''
    Sqoop_Import operation
    
    @param dt 昨天的日期，将由调度模块传入
    '''
    def __init__(self, dt):
        # 格式化dt参数，给dt: '2019-05-18' 字符串套上引号
        self.dt = '\"' + dt + '\"'
        # 设置Sqoop 操作类型为导入
        self.sqoop_type = self.IMPORT
    
    # 实现自定义 任务类型处理
    def task_type_resolve_customize(self, task_type):
        #获得导入类型，增量导入或者全量导入
        import_type = task_type
        #如果为全量导入
        if (import_type == "all"):
            #query的查询条件为<dt
            self.import_condition = "< " + self.dt
        #如果为增量导入
        elif (import_type == "add"):
            #query的查询条件为=dt
            self.import_condition = "= " + self.dt
        else:
            raise Exception
    
    # 实现自定义键值对处理
    def key_value_resolve_customize(self, key, value):
        #将query的CONDITIONS替换为查询条件
        if (key == "query"):
            value = value.replace("\$CONDITIONS", self.import_condition)
            
        #将导入分区替换为传入的时间
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
    # python import.py dt   第一个参数argv[0]为import.py 第二个参数argv[1]为dt
    dt = sys.argv[1]
    print("dt:" + dt + "\n")
    
    Sqoop_Import(dt).driver()