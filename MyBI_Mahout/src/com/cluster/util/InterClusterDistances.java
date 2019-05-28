package com.cluster.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;

public class InterClusterDistances {
	
	/*平均簇间距离计算，衡量聚类质量*/
	
	private static String inputFile = "";
	
	public static void prasePra(String[] args) {
		inputFile = args[0];
		System.out.println("聚类结果文件地址：" + inputFile);
	}
	
	public static void main(String[] args) throws IOException, 
		InstantiationException, IllegalAccessException{
		
		prasePra(args);
		Configuration conf = new Configuration();
		Path path = new Path(inputFile);
		System.out.println("Input Path: " + path);
		FileSystem fs = FileSystem.get(path.toUri(), conf);
		path = path.makeQualified(fs.getUri(), fs.getWorkingDirectory());
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		//读取聚类结果文件地址（HDFS上的）
		SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(path));
		Writable key = (Writable) reader.getKeyClass().newInstance();
		ClusterWritable value = (ClusterWritable) reader.getValueClass().newInstance();
		
		//循环读取文件
		while (reader.next(key, value)) {
			Cluster cluster = value.getValue();
			clusters.add(cluster);
			value = (ClusterWritable) reader.getValueClass().newInstance();
		}
		
		System.out.println("Cluster In Total: " + clusters.size());
		
		DistanceMeasure measure = new EuclideanDistanceMeasure();
		
		double max = 0;
		double min = Double.MAX_VALUE;
		double sum = 0;
		int count = 0;
		
		//如果聚类的个数大于1才开始计算
		if(clusters.size() != 1 && clusters.size() != 0) {
			for (int i = 0; i < clusters.size(); i++) {
				for (int j = i + 1; j < clusters.size(); j++) {
					double d = measure.distance(clusters.get(i).getCenter(), 
							clusters.get(j).getCenter());
					min = Math.min(d, min);
					max = Math.max(d, max);
					sum += d;
					count++;
				}
			}
			
			System.out.println("Maximum Intercluster Distance: " + max);
			System.out.println("Minimum Intercluster Distance: " + min);
			//System.out.println("Average Intercluster Distance: " + (sum / count - min)/ (max - min));
			System.out.println("Average Intercluster Distance: " + sum / count);
			
		} else if (clusters.size() == 1) {
			System.out.println("只有一个类，无法判断聚类质量");
		} else if (clusters.size() == 0) {
			System.out.println("聚类失败");
		}
		
		reader.close();
	}
	
}
