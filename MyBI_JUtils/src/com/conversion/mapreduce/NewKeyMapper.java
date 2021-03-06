package com.conversion.mapreduce;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class NewKeyMapper extends Mapper<LongWritable, Text, Text, Text>{
	
	private static final String SEPARATOR = "@";
	
	private String[] desUrlsRegex = null;
	
	//用正则的方式判断是否相等
	public static boolean regex(String value, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(value);
		return m.find();
	}
	
	@Override
	protected void map(LongWritable key, Text value, Context context)
		throws IOException, InterruptedException {
		
		//从Context对象中取得表示漏斗的url的正则表达式
		if (desUrlsRegex == null) {
			desUrlsRegex = context.getConfiguration().get("urls").split(SEPARATOR);
			//如果表示漏斗的url为空，则返回
			if (desUrlsRegex == null ) {
				return;
			}
			for (int i = 0; i < desUrlsRegex.length; i++) {
				System.out.println(desUrlsRegex[i]);
			}
		}
		
		//表示conversion_input表中的一行，按照分隔符切开
		String[] logInfos = value.toString().split("\t");
		System.out.println("value: " + value.toString());
		//获取url
		String url = logInfos[0];
		
		//记录未访问目标地址的数目标记
		int flag = 0;
		for (int i = 0; i < desUrlsRegex.length; i++) {
			if (regex(url, desUrlsRegex[i])) {
				break;  
			} else {
				flag += 1;
			}
		}
		//如果该记录未访问目标地址则丢弃
		if (flag == desUrlsRegex.length) {
			return;
		}
		
		//获取用户的唯一id
		String uuid = logInfos[1];
		//获取sessionId
		String sessionId = logInfos[2];
		
		try {
			//获取csvp
			int csvp = Integer.parseInt(logInfos[3]);
			//将sessionId和csvp组合成新的key
			String newKey = sessionId + SEPARATOR + csvp;
			//剩下的部分作为新的value
			String newValue = uuid + SEPARATOR + url;
			//输出
			context.write(new Text(newKey), new Text(newValue));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
