package com.etl.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.etl.utls.IpParser4LocalCache;
import com.etl.utls.LogParser;
import com.etl.utls.LogParserFactory;


public class ClickStreamMapper extends Mapper<LongWritable, Text, Text, Text> {
	
	//本地缓存文件（从HDFS缓存到本地，与任务同目录）
	private static String localCacheFile = "qqwry.dat";//缓存文件，直接按文件名获取
	
	@Override
	protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		super.setup(context);
		
		//设置IP解析器访问本地缓存数据库文件
		IpParser4LocalCache.setDbPath(localCacheFile);
	}
	
	@Override
	protected void map(LongWritable key, Text value, Context context) 
			throws IOException, InterruptedException {
		//注：此map的输入key为文本行号，value为单行文本
		String log = value.toString();
		
		//获取当前Mapper对象注册的LogParser对象，解析log日志
		LogParser logParser = LogParserFactory.getInstance();
		logParser.parse(log);
		
		//用sessionId和receiveTime组成新的key
		String mapOutKey = logParser.getSessionId() + "&" + logParser.getReceiveTime();
		//按照clickstream_log表的顺序重新组合这些字段
		String mapOutValue = logParser.getIpAddress() + "\t" + logParser.getUniqueId() + "\t" 
				+ logParser.getUrl() + "\t" + logParser.getSessionId() + "\t" 
				+ logParser.getSessionTimes() + "\t" + logParser.getAreaAddress() + "\t" 
				+ logParser.getLocalAddress() + "\t" + logParser.getBrowserType() + "\t" 
				+ logParser.getOperationSys() + "\t" + logParser.getReferUrl() + "\t" 
				+ logParser.getReceiveTime() + "\t" + logParser.getUserId();
		
		context.write(new Text(mapOutKey), new Text(mapOutValue));
	}
	
}
