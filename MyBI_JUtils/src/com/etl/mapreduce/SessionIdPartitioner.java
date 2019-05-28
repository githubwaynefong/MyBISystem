package com.etl.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class SessionIdPartitioner extends Partitioner<Text, Text> {
	/*
	 * 控制shuffle，实现按照sessionId分发的规则，将同一sessionId的key放到一块，方便排序
	 */
	
	@Override
	public int getPartition(Text key, Text value, int parts) {
		
		String sessionId = "-";
		
		if (key != null) {
			//得到sessionId
			sessionId = key.toString().split("&")[0];
		}
		
		//将sessionId从0到Integer的最大值散列
		int num = (sessionId.hashCode() & Integer.MAX_VALUE) % parts;
		
		return num;
	}
	
}
