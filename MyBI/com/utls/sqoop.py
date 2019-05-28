# -*- coding:UTF-8 -*-
from com.utls.pro_env import PROJECT_CONF_DIR
from com.utls.pro_env import PROJECT_CONF_TABLES_DIR
from xml.etree import ElementTree
import commands
import re

'''
Sqoop导入导出<模板类>工具

1.导入导出需继承此类；
2.选择性实现自定义的 任务类型处理方式 和 键值对处理方式：
task_type_resolve_customize(self, task_type)
key_value_resolve_customize(self, key, value)

'''
class SqoopUtil(object):
    
    IMPORT = 'Import'
    EXPORT = 'Export'
    sqoop_type = None
    
    '''
    sqoop operation
    '''
    def __init__(self):
        pass

    # 导入表的配置解析
    def resolve_conf(self):

        # 判断Sqoop操作类型：0|Import or 1|Export
        if self.sqoop_type == self.IMPORT:
            node_index = 0
        elif self.sqoop_type == self.EXPORT:
            node_index = 1
        else:
            raise Exception
        
        #获得配置文件名
        conf_file = PROJECT_CONF_DIR + self.sqoop_type + ".xml"
        #解析配置文件
        xml_tree = ElementTree.parse(conf_file)
        #获得task元素
        tasks = xml_tree.findall('./task')
        
        for task in tasks:
            
            # 实现自定义 任务类型处理
            self.task_type_resolve_customize(task.attrib["type"])
            
            #获得表名集合
            tables = task.findall('./table')
            
            #用来保存待执行的Sqoop命令的集合
            cmds = []
            
            #迭代表名集合，解析表配置文件
            for i in range(len(tables)):
                
                #表名
                table_name = tables[i].text
                #表配置文件名
                table_conf_file = PROJECT_CONF_TABLES_DIR + table_name + ".xml"
                
                #解析表配置文件
                xmlTree = ElementTree.parse(table_conf_file)
                
                #获取sqoop-shell结点
                sqoopNodes = xmlTree.findall("./sqoop-shell")
                
                #获取sqoop命令类型
                sqoop_cmd_type = sqoopNodes[node_index].attrib["type"]
                #获取参数结点
                praNodes = sqoopNodes[node_index].findall("./param")
                
                #用来保存param的信息的字典
                cmap = {}
                for i in range(len(praNodes)):
                    #获得key属性的值
                    key = praNodes[i].attrib["key"]
                    #获得param标签中间的值
                    value = praNodes[i].text
                    #保存到字典中
                    cmap[key] = value
                    
                #首先组装成sqoop命令头
                command = "sqoop " + " " + sqoop_cmd_type
                
                #迭代字典将param的信息拼装成字符串
                for key in cmap.keys():
                    
                    value = cmap[key]
                    
                    #如果不是键值对形式的命令选项
                    if (value == None or value == "" or value == " "):
                        value = ""
                    
                    # 实现自定义键值对处理
                    key, value = self.key_value_resolve_customize(key, value)
                    
                    #拼装为命令
                    command += " --" + key + " " + value
                        
                #将命令加入至待执行命令集合
                cmds.append(command)
            #输出每个task的cmds
            #for i in range(len(cmds)):
            #    print(cmds[i])
        
        return cmds
    
    
    # 实现自定义 任务类型处理 
    def task_type_resolve_customize(self, task_type):
        pass
    
    # 实现自定义键值对处理
    def key_value_resolve_customize(self, key, value):
        return key, value
    
    
    #Python调用Sqoop命令函数
    def execute_shell(self, shell):
        
        # 格式化shell语句
        shell = self.format_shell(shell)
        
        # 将传入的shell命令执行
        # 测试：shell = 'sqoop list-databases --connect jdbc:mysql://master:3306/ --username hadoop -password hivepwd'
        status, output = commands.getstatusoutput(shell)

        if status != 0:
            print "Sqoop execute failed :\n"
            print shell
            print output
            return None
        else:
            print "Sqoop execute success :\n"
            print shell
            print output
        
        output = str(output).split("\n")
        
        return output
    
    
    # 将shell语句格式化，去除\n\t等字符，以免无法执行shell
    def format_shell(self, shell):
        
        # 将多个连续的空白字符（等价于[\t\n\r\f]）替换成单个空格
        shell = re.sub(r'(\s+)', r' ', shell)
        
        # 注：'\\t'中'\\'属于一个整体，代表转义字符'\'，所以'\\t'不会被当作'\t'替换掉
        # print re.sub(r'(\s+)', r' ', '\\t1\t2    ')
        
        return shell
    
    
    # Sqoop 调度模块
    def driver(self):
        
        print(self.sqoop_type + "模块开始执行.....\n")
        
        print(self.sqoop_type + ".xml配置文件解析开始.....\n")
        #解析配置文件，获得sqoop命令集合
        cmds = self.resolve_conf()
        print(self.sqoop_type + ".xml配置文件解析结束.....\n")
        
        print("sqoop命令开始执行.....\n")
        #迭代集合，执行命令
        for i in range(len(cmds)):
            
            cmd = cmds[i]
            
            #执行导入过程
            self.execute_shell(cmd)
        print("sqoop命令结束执行.....\n")
        
        print(self.sqoop_type + "模块结束执行.....\n")
