package com.mahout;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.canopy.CanopyDriver;
import org.apache.mahout.clustering.conversion.InputDriver;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.utils.clustering.ClusterDumper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCluster {
	private static final Logger log = LoggerFactory.getLogger(UserCluster.class);
	
	/*
	 * @param args[0]:outputPath mahout的输出至HDFS的目录
	 * @param args[1]:inputPath mahout的输入目录，为cluster_input表的HDFS目录
	 * @param args[2]:t1 Canopy算法的距离阈值t1（外圈，t1 > t2）
	 * @param args[3]:t2 Canopy算法的距离阈值t2（内圈）
	 * @param args[4]:convergenceDelta 收敛阈值
	 * @param args[5]:maxIterations 最大迭代次数
	 */
	public static void main(String[] args) throws Exception {
		
		//mahout的输出至HDFS的目录
		String outputPath = args[0];
		//mahout的输入目录，为cluster_input表的HDFS目录
		String inputPath = args[1];
		//Canopy算法的距离阈值t1（外圈，t1 > t2）
		double t1 = Double.parseDouble(args[2]);
		//Canopy算法的距离阈值t2（内圈）
		double t2 = Double.parseDouble(args[3]);
		//收敛阈值
		double convergenceDelta = Double.parseDouble(args[4]);
		//最大迭代次数
		int maxIterations = Integer.parseInt(args[5]);
		
		Path output = new Path(outputPath);
		Path input = new Path(inputPath);
		Configuration conf = new Configuration();
		//在每次执行聚类前，删除掉上一次的输出目录
		HadoopUtil.delete(conf, output);
		//执行聚类
		run(conf, input, output, new EuclideanDistanceMeasure()
				,t1, t2, convergenceDelta, maxIterations);
	}
	
	public static void run(Configuration conf, Path input, Path output
			, DistanceMeasure measure, double t1, double t2
			, double convergenceDelta, int maxIterations)
		throws Exception {
		
		Path directoryContainingConvertedInput = new Path(output, "data");
		
		log.info("Preparing Input");
		//将输入文件序列化，并选取RandomAccessSparseVector作为保存向量的数据结构
		InputDriver.runJob(input, directoryContainingConvertedInput
				, "org.apache.mahout.math.RandomAccessSparseVector");
		
		log.info("Running Canopy to get initial clusters");
		//保存canopy的目录
		Path canopyOutput = new Path(output, "canopies");
		//执行Canopy聚类
		CanopyDriver.run(conf, directoryContainingConvertedInput, canopyOutput
				, measure, t1, t2, false, 0.0, false);
		
		log.info("Running KMeans");
		//执行k-means聚类，并使用canopy的目录
		KMeansDriver.run(conf, directoryContainingConvertedInput, 
				new Path(canopyOutput, Cluster.INITIAL_CLUSTERS_DIR + "-final")
				, output, measure, convergenceDelta, maxIterations, true, 0.0, false);
		
		log.info("run clusterdumper");
		//将聚类的结果输出至HDFS
		ClusterDumper clusterDumper = new ClusterDumper(new Path(output, "cluster-*-final")
				, new Path(output, "clustered_points"));
		clusterDumper.printClusters(null);
	}
}
