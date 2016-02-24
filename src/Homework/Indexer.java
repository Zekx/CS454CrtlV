package Homework;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.fs.Path;

public class Indexer {
	
	public static class Map extends MapReduceBase implements Mapper<Object, Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable();
		private Text word = new Text();

		@Override
		public void map(Object arg0, Text arg1, OutputCollector<Text, IntWritable> arg2, Reporter arg3)throws IOException {
			
			String line = arg1.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while(tokenizer.hasMoreTokens()){
				word.set(tokenizer.nextToken());
				arg2.collect(word, one);
			}
			
		}
		
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable>{

		@Override
		public void reduce(Text arg0, Iterator<IntWritable> arg1, OutputCollector<Text, IntWritable> arg2, Reporter arg3) throws IOException {
			int sum = 0;
			while(arg1.hasNext()){
				sum += arg1.next().get();
			}
			
			arg2.collect(arg0, new IntWritable(sum));
		}
		
	}
	
	public static void run(String path){
		System.out.println(path);
		
		JobConf conf = new JobConf(Indexer.class);
		conf.setJobName("indexer");
		conf.set("xmlinput.start", "<tag>");
		conf.set("xmlinput.end", "</tag>");
			
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
		
		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
			
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(conf, path);
		FileOutputFormat.setOutputPath(conf, new Path("C:\\data"));
			
		try {
			JobClient.runJob(conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
