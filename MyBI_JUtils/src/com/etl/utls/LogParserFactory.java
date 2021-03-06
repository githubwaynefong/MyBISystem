package com.etl.utls;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogParserFactory {
	
	//简单单例模式
	private static LogParser logParser = null;
	public static LogParser getInstance() {
		if (logParser != null) {
			return logParser;
		}
		else {
			try {
				//反射创建对象
				Class LogParserClass = Class.forName(LogParser.class.getName());
				Constructor constructor = LogParserClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				LogParser logParser = (LogParser) constructor.newInstance();
				
				return logParser;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	//日志解析对象按线程注册（一个线程对应一个解析对象）
	public static Map<Thread, LogParser> threadLogParserMap = new ConcurrentHashMap<Thread, LogParser>();
	
	//单利模式获取当前线程对应的日志解析对象
	public static LogParser getInstance(Thread thread) {
		if (threadLogParserMap.containsKey(thread)) {
			LogParser logParser = threadLogParserMap.get(thread);
			return logParser;
		}
		else {
			try {
				//反射创建对象
				Class LogParserClass = Class.forName(LogParser.class.getName());
				Constructor constructor = LogParserClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				LogParser logParser = (LogParser) constructor.newInstance();
				//map插入
				threadLogParserMap.put(thread, logParser);
				
				return logParser;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static void testThreadLogParser() {
		Thread thread = null;
		for (int i = 0; i < 5; i++) {
			
			thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					LogParser logParser = null;
					for (int i = 0; i < 2; i++) {
						//解析log日志
						logParser = LogParserFactory.getInstance(Thread.currentThread());
						logParser.parse(LogParser.LOG);
						//验证是否发生map.put()时的碰撞丢失（多线程时HashMap不安全）
						System.out.println("Current Thread: " + Thread.currentThread().getName()
						+ "\tCurrent LogParser Index: " + logParser.getIndex()
						+ "\tis equal: " + logParser.equals(
								LogParserFactory.threadLogParserMap.get(Thread.currentThread())));
					}
				}
				
			}, "Thread-" + i);
			thread.start();
		}
	}
	
	
	//日志解析对象按对象注册（一个调用对象对应一个解析对象）
	public static Map<Object, LogParser> objectLogParserMap = new ConcurrentHashMap<Object, LogParser>();
	
	//单利模式获取当前对象对应的日志解析对象
	public static LogParser getInstance(Object obj) {
		if (objectLogParserMap.containsKey(obj)) {
			LogParser logParser = objectLogParserMap.get(obj);
			return logParser;
		}
		else {
			try {
				//反射创建对象
				Class LogParserClass = Class.forName(LogParser.class.getName());
				Constructor constructor = LogParserClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				LogParser logParser = (LogParser) constructor.newInstance();
				//map插入
				objectLogParserMap.put(obj, logParser);
				
				return logParser;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static void testObjLogParser() {
		//调用LogParser对象的对象
		class MyObj {
			private String name;
			public MyObj(String name) {this.name = name;}
			public String getName() {return this.name;}
			public void driver() {
				LogParser logParser = null;
				//每个MyObj多次执行解析操作，重用解析对象
				for (int i = 0; i < 2; i++) {
					//解析log日志
					logParser = LogParserFactory.getInstance(this);
					logParser.parse(LogParser.LOG);
					//验证是否发生map.put()时的碰撞丢失（多线程时HashMap不安全）
					System.out.println("Current Object: " + this.getName()
					+ "\tCurrent LogParser Index: " + logParser.getIndex()
					+ "\tis equal: " + logParser.equals(LogParserFactory.objectLogParserMap.get((Object) this)));
				}
			}
		}
		//创建5个线程，每个线程发起两个不同的MyObj对象操作LogParser
		Thread thread = null;
		for (int i = 0; i < 5; i++) {
			thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					for (int i = 0; i < 2; i++) {
						new MyObj(Thread.currentThread().getName() + "-obj-" + i).driver();
					}
				}
				
			}, "Thread-" + i);
			thread.start();
		}
	}
	
	public static void main(String[] args) {
		
//		testThreadLogParser();
//		testObjLogParser();
	}

}
