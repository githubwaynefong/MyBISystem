package com.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.math.RandomAccessSparseVector;

public class ClusterOutput {
	
	/*
	 * @param args[0]:clusterOutputPath Mahout的输出文件，需要被解析
	 * @param args[1]:resultPath 解析后的聚类结果文件，将输出至本地磁盘
	 */
	public static void main(String[] args) {
		
		try {
			
			//Mahout的输出文件，需要被解析
			String clusterOutputPath = args[0];
			//解析后的聚类结果文件，将输出至本地磁盘
			String resultPath = args[1];
			
			BufferedWriter bw = null;
			
			Configuration conf = new Configuration();
			conf.set("fs.default.name", "hdfs://master:9000");
			FileSystem fs = FileSystem.get(conf);
			
			Path file = new Path(clusterOutputPath + "/clustered_points/part-m-00000");
			file = file.makeQualified(fs.getUri(), fs.getWorkingDirectory());
			
			SequenceFile.Reader reader = null;
			reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(file));
			
			bw = new BufferedWriter(new FileWriter(new File(resultPath)));
			
			//key为聚簇中心id
			IntWritable key = new IntWritable();
			WeightedVectorWritable value = new WeightedVectorWritable();//带权向量
			
			while (reader.next(key, value)) {
				//得到向量（随机存取的稀疏向量，仅含有非零元素）
				RandomAccessSparseVector vector = (RandomAccessSparseVector) value.getVector();
				
				String vectorValue = "";
				
				//将向量各个维度拼接成一行，用\t分隔
				for (int i = 0; i < vector.size(); i++) {
					if (i == vector.size() - 1) {
						vectorValue += vector.get(i);
					} else {
						vectorValue += vector.get(i) + "\t";
					}
				}
				
				//在向量前加上该向量属于的聚簇中心id
				bw.write(key.toString() + "\t" + vectorValue + "\n");
			}
			
			bw.flush();
			bw.close();
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
