package Homework;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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

import Homework.Threads.DeskThreads;

public class DesktopCrawler implements Runnable{
		DeskThreads desk;
	
		public DesktopCrawler(DeskThreads desk){
			this.desk = desk;
		}
		
		@Override
		public void run(){
			//Connects to the Mongo Database.
	        MongoClient mongoClient = new MongoClient("ec2-52-36-142-197.us-west-2.compute.amazonaws.com", 27017);
	        DB db = null;

	        System.out.println("Establishing connection...");

	        //Get the connection.
	        db = mongoClient.getDB("crawler");
	        DBCollection table = db.getCollection("urlpages");
	        table.createIndex(new BasicDBObject("hash", 1));
			DBCollection index = db.getCollection("index");
			table.createIndex(new BasicDBObject("word", 1));
			DBCollection pageRank = db.getCollection("pagerank");
			pageRank.createIndex(new BasicDBObject("Hash", 1));
			
			this.crawlFiles(desk.file, desk.ext, db, table, desk.url);
		}

		public static JSONArray getLinks(String url, String uri, File file) throws IOException
	    {
	    	JSONArray arr = new JSONArray();
	    	Document doc = Jsoup.parse(file, StandardCharsets.UTF_8.toString(), url);
	    	
	    	Elements allLinks = doc.select("a[href]");
	    	//System.out.println(url);
	        for (Element link : allLinks) {
	            if (link != null) 
	            {
	            	//System.out.println(link.attr("href"));
	            	int counter = 0;
            		String[] split1 = link.attr("href").split("/");
            		//System.out.println(link.attr("href"));
            		String refinedUrl = new String(link.attr("href")
            				.replace("../", "")
            				.replace("%7E", "~")
            				.replace("%21", "!")
            				.replace("%22", "\"")
            				.replace("%23", "#")
            				.replace("%24", "$")
            				.replace("%25", "%")
            				.replace("%26", "&")
            				.replace("%27", "'")
            				.replace("%28", "(")
            				.replace("%29", ")")
            				.replace("%2A", "*")
            				.replace("%2B", "+")
            				.replace("%2C", ",")
            				.replace("%2D", "-")
            				.replace("%2E", ".")
            				.replace("%2F", "/")
            				.replace("%5B", "[")
            				.replace("%5C", "\\")
            				.replace("%5D", "]")
            				.replace("%5E", "^")
            				.replace("%5F", "_")
            				.replace("%C3%80", "À")
            				.replace("%C3%81", "Á")
            				.replace("%C3%82", "Â")
            				.replace("%C3%83", "Ã")
            				.replace("%C3%84", "Ä")
            				.replace("%C3%85", "Å")
            				.replace("%C3%86", "Æ")
            				.replace("%C3%87", "Ç")
            				.replace("%C3%88", "È")
            				.replace("%C3%89", "É")
            				.replace("%C3%8A", "Ê")
            				.replace("%C3%8B", "Ë")
            				.replace("%C3%8C", "Ì")
            				.replace("%C3%8D", "Í")
            				.replace("%C3%8E", "Î")
            				.replace("%C3%8F", "Ï")
            				.replace("%C3%A1", "á")
            				.replace("%C3%A2", "â")
            				.replace("%C3%A3", "ã")
            				.replace("%C3%A4", "ä")
            				.replace("%C3%A5", "å")
            				.replace("%C3%A6", "æ")
            				.replace("%C3%A7", "ç")
            				.replace("%C3%A8", "è")
            				.replace("%C3%A9", "é")
            				.replace("%C3%AA", "ê")
            				.replace("%C3%AB", "ë")
            				.replace("%C3%AC", "ì")
            				.replace("%C3%AD", "í")
            				.replace("%C3%AE", "î")
            				.replace("%C3%AF", "ï")
            				.replace("%E2%80%98", "‘")
            				.replace("%E2%80%99", "’")
            				.replace("%E2%80%9C", "“")
            				.replace("%E2%80%9D", "”")
            				.replace("%60", "`")
            				.getBytes("UTF-8"));
            		
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
            		//System.out.println(new String(finalizedurl.getBytes("UTF-8")));
            		arr.add(new String(finalizedurl.getBytes("UTF-8")));
            		//System.out.println(finalizedurl);
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
	                    	
	                    	if(!desk.visitedAlready.contains(url+"/"+file.getName())){
	                    		synchronized(desk.lock){
	                    			desk.visitedAlready.add(url+"/"+file.getName());
	                    			//System.out.println(url+"/"+file.getName());
	                    		}
	                    		JSONArray dataSet = ext.extract(file);

		                        JSONObject metadata = ext.extractMeta(file);
		                        
		                        ext.exportJson(new File(url+"/"+file.getName()), file.getName(), url+"/"+file.getName(), dataSet, metadata, table, getLinks(url, metadata.get("Content-Encoding").toString() ,file));
		                        ext.indexTerms(db, ext.SHA256Converter(url+"/"+file.getName()), file);
		                        System.out.println(desk.visitedAlready.size());
	                    	}
	                    }

	                } catch (Exception e) {
	                    e.printStackTrace();
	                    System.out.println(System.getProperty("user.dir"));
	                }
	            }
	        }
	    }

	    public static void main(String[] args) throws UnknownHostException {
	    	//Connects to the Mongo Database.
	        MongoClient mongoClient = new MongoClient("ec2-52-36-142-197.us-west-2.compute.amazonaws.com", 27017);
	        DB db = null;
	        //Get the connection.
	        db = mongoClient.getDB("crawler");
	        
	        System.out.println("Connected to MongoDB!");
//	        db.getCollection("urlpages").drop();
//	        db.getCollection("index").drop();
//	        db.getCollection("pagerank").drop();
	         
	        Extractor ext = new Extractor();
	        File[] files = new File("./en").listFiles();
	        
	        DeskThreads desk = new DeskThreads(30, files, ext, "C:/data/en");
	        desk.run();

	        Ranking ranker = new Ranking(db);
	        ranker.link_analysis();
	        ranker.TFIDF("discovery");
	    }

	}

