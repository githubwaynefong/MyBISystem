package com.etl.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;



public class Driver {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) 
		throws IOException, ClassNotFoundException, InterruptedException {
		
		Configuration configuration = new Configuration();
		
		if (args.length != 2) {
			System.out.println("参数不正确");
			return;
		}
		
		//取得输入路径，即点击流日志存放的HDFS路径
		String inputPath = args[0];
		//取得输出路径，即Clickstream_log表的HDFS路径，需要考虑其分区路径
		String outputPath = args[1];
		
		//分布式缓存文件路径（计算节点使用的IP地址数据库文件）
		String cachePath[] = {
				"hdfs://master:9000/user/hadoop/cz88/qqwry.dat"
		};
		
		Job job = Job.getInstance(configuration, "clickstream_etl");	//new Job()已过时
		job.setJarByClass(Driver.class);
		//向分布式缓存中添加文件
        job.addCacheFile(new Path(cachePath[0]).toUri());
        //作业输入输出路径
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		job.setMapperClass(ClickStreamMapper.class);
		job.setReducerClass(ClickStreamReducer.class);
		//手动设置Reducer的个数，该值可根据集群计算能力酌情考虑
		job.setNumReduceTasks(1);
		//设置作业输出
		job.setOutputFormatClass(TextOutputFormat.class);
		//自定义分区、排序
		job.setPartitionerClass(SessionIdPartitioner.class);
		job.setSortComparatorClass(SortComparator.class);
		//设置reduce输出
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}
}
