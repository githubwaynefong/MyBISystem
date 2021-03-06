package com.etl.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ClickStreamReducer extends Reducer<Text, Text, NullWritable, Text> {
	
	//表示前一个sessionId
	private String preSessionId = "-";
	//排序号
	private int csvp = 0;
	
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		//注：key由sessionId & receiveTime 组合的，同一个sessionId的receiveTime都不同（由先后顺序）
		//   所以传入的key都是唯一的，没有重复，每一个key对应的values也就只有一个值
		
		String sessionId = key.toString().split("&")[0];
		
		//如果与前一个sessionId相同，说明是同一个session
		if (preSessionId.equals(sessionId)) {
			//累加csvp
			csvp++;
		} 
		//如果不同，说明是第一条数据session或者是新的session，重置preSessionId和csvp
		else {
			preSessionId = sessionId;
			csvp = 1;
		}
		
		//按照clickstream_log的格式在末尾加上csvp
		String reduceOutValue = values.iterator().next().toString() + "\t" + csvp;
		//只输出value，key已经包含在value中
		context.write(NullWritable.get(), new Text(reduceOutValue));
	}
	
}
