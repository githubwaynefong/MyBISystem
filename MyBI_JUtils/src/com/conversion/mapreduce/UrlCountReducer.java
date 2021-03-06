package com.conversion.mapreduce;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class UrlCountReducer extends Reducer<Text, Text, NullWritable, Text>{
	
	//表示前一条记录的sessionId
	public static String preSessionId = "not set";
	//表示漏斗的进度（步骤），如1为漏斗的第一步
	public static int process = 0;
	public static final String SEPARATOR = "@";
	
	public static boolean regex(String value, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(value);
		return m.find();
	}
	
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
		throws IOException, InterruptedException {
		
		//从Context对象中取得表示漏斗的url正则表达式
		String[] desUrls = context.getConfiguration().get("urls").split(SEPARATOR);
		//取得sessionId
		String sessionId = key.toString().split(SEPARATOR)[0];
		String value = values.iterator().next().toString();
		//取得url
		String url = value.split(SEPARATOR)[1];
		//取得uuid
		String uuid = value.split(SEPARATOR)[0];
		
		/*
		 * 当：  1.preSessionId = sessionId 
		 *  并且：2.进度小于漏斗模型总进度（说明正在进行漏斗的比较中，一轮进度还未完成）
		 * 时
		 */
		if (preSessionId.equals(sessionId) && process < desUrls.length) {
			
			//按漏斗模型进度匹配符合的url
			if (regex(url, desUrls[process])) {
				process++; //更新进度
				//输出的格式为：sessionId + uuid + 漏斗的进度
				String result = sessionId + "\t" + uuid + "\t" + process;
				context.write(NullWritable.get(), new Text(result));
			}
			//丢弃不符合的url记录
			else
				return;
		} 
		/*
		 * 如果是：1.第一次执行reduce函数
		 * 	或者： 2.一个新sessionId
		 *  或者：3.一个漏斗比较完成
		 * 时
		 */
		else {
			//重置preSsessionId和进度process
			preSessionId = sessionId;
			process = 0;
			//匹配漏斗的第一个步骤
			if (regex(url, desUrls[0])) {
				process = 1;
				//输出格式为：sessionId + uuid + 漏斗的进度
				String result = sessionId + "\t" + uuid + "\t" + process;
				context.write(NullWritable.get(), new Text(result));
			} 
			//丢弃不符合的url记录
			else {
				return;
			}
		}
	}
}
