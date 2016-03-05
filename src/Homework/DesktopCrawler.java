package Homework;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class DesktopCrawler implements Runnable{
		DeskThreads desk;
	
		public DesktopCrawler(DeskThreads desk){
			this.desk = desk;
		}
		
		@Override
		public void run(){
			//Connects to the Mongo Database.
	        MongoClient mongoClient = new MongoClient("localhost", 27017);
	        DB db = null;

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
			DBCollection index = db.getCollection("index");
			DBCollection pageRank = db.getCollection("pagerank");
			
			this.crawlFiles(desk.file, desk.ext, db, table, desk.url);
		}

		public static JSONArray getLinks(String url, String uri, File file) throws IOException
	    {
	    	JSONArray arr = new JSONArray();
	    	Document doc = Jsoup.parse(file, uri, url);
	    	
	    	Elements allLinks = doc.select("a[href]");
	    	//System.out.println(url);
	        for (Element link : allLinks) {
	            if (link != null) 
	            {
	            	//System.out.println(link.attr("href"));
	            	if(link.attr("abs:href").contains("ctrlv")){
	            		int counter = 0;
	            		String[] split1 = link.attr("href").split("/");
	            		//System.out.println(link.attr("href"));
	            		String refinedUrl = link.attr("href").replace("../", "");
	            		
	            		for(String s: split1){
	            			//System.out.println(s);
	            			if(s.equals("..")){
	            				counter++;
	            			}
	            		}
	            		counter++;
	            		
	            		String[] split2 = file.toURL().toString().split("/");

	            		for(int i = split2.length-1; counter > 0; i--){
	            			//System.out.println(split2[i]);
	            			split2[i] = "";
	            			counter--;
	            		}

	            		String finalizedurl = "http://www.ctrlv.com";
	            		for(int t = 3; t < split2.length; t++){
	            			//System.out.println(split2[t]);
	            			if(!split2[t].equals("")){
	            				finalizedurl = finalizedurl + "/" + split2[t];
	            			}
	            		}
	            		finalizedurl = finalizedurl + "/" + refinedUrl;
	            		arr.add(finalizedurl);
	            		//System.out.println(finalizedurl);
	            	}
	            }
	        }
	    	return arr;
	    }
	
		public void crawlFiles(File[] files, Extractor ext,DB db, DBCollection table, String url) {
	        if (files != null) {
	           
	            for (File file : files) {
	            	//System.out.println(file.toString());
	                Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());

	                try {
	                    if (file.isDirectory()) {
	                    	String nexturl = url + "/" + file.getName();
	                    	crawlFiles(file.listFiles(), ext, db, table, nexturl);

	                    } else {
	                    	try{
	                    		
	                    	}catch(Exception e){
	                    		e.printStackTrace();
	                    	}
	                    	if(!desk.visitedAlready.contains(url+file.getName())){
	                    		synchronized(desk.lock){
	                    			desk.visitedAlready.add(url+file.getName());
	                    		}
	                    		JSONArray dataSet = ext.extract(file);

		                        JSONObject metadata = ext.extractMeta(file);
		                        
		                        ext.exportJson(file, file.getName(), url+file.getName(), dataSet, metadata, table, getLinks(url, metadata.get("Content-Encoding").toString() ,file));
		                        ext.indexTerms(db, file.toString().hashCode(), file);
		                        System.out.println(desk.visitedAlready.size());
	                    	}
	                    }

	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

	    public static void main(String[] args) throws UnknownHostException {
	    	//Connects to the Mongo Database.
	        MongoClient mongoClient = new MongoClient("localhost", 27017);
	        DB db = null;

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
			DBCollection index = db.getCollection("index");
			DBCollection pageRank = db.getCollection("pagerank");

	        System.out.println("Connected to MongoDB!");
	        db.getCollection("urlpages").drop();
	        db.getCollection("index").drop();
	        db.getCollection("pagerank").drop();
	        
	        Extractor ext = new Extractor();
	        File[] files = new File("C:/data/en").listFiles();
	        
	        DeskThreads desk = new DeskThreads(15, files, ext, "http://www.ctrlv.com/en");
	        desk.run();

	        Ranking ranker = new Ranking(db);
	        ranker.link_analysis();
	        ranker.TFIDF("google");
	    }

	}

