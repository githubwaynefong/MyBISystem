package com.conversion.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class SessionIdPartitioner extends Partitioner<Text, Text>{
	
	public static final String SEPARATOR = "@";
	
	@Override
	public int getPartition(Text key, Text value, int parts) {
		
		String sessionId = "-";
		if (key != null) {
			//得到sessionId
			sessionId = key.toString().split(SEPARATOR)[0];
		}
		
		//将sessionId从0到Integer的最大值散列
		int reducerNum = (sessionId.hashCode() & Integer.MAX_VALUE) % parts;
		
		return reducerNum;
	}
}
