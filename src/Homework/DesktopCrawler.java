package Homework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
		public Map<String, String> decode;
	
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
	    	Document doc = Jsoup.parse(file, StandardCharsets.UTF_8.toString(), "http://www.ctrlv.com");
	    	
	    	Elements allLinks = doc.select("a[href]");
	    	//System.out.println(url);
	    	int i;
	    	Element link;
	        for (i=0;i<allLinks.size();i++) {
	        	link = allLinks.get(i);
	        	
	            if (link != null) 
	            {
	            	//System.out.println(link.attr("href"));
	            	if(link.attr("abs:href").contains("www.ctrlv.com")){
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
	            				.replace("%CE%84", "΄")
	            				.replace("%CE%85", "΅")
	            				.replace("%CE%86", "Ά")
	            				.replace("%CE%87", "·")
	            				.replace("%CE%88", "Έ")
	            				.replace("%CE%89", "Ή")
	            				.replace("%CE%8A", "Ί")
	            				.replace("%CE%8C", "Ό")
	            				.replace("%CE%8E", "Ύ")
	            				.replace("%CE%8F", "Ώ")
	            				.replace("%CE%90", "ΐ")
	            				.replace("%CE%91", "Α")
	            				.replace("%CE%92", "Β")
	            				.replace("%CE%93", "Γ")
	            				.replace("%CE%94", "Δ")
	            				.replace("%CE%95", "Ε")
	            				.replace("%CE%96", "Ζ")
	            				.replace("%CE%97", "Η")
	            				.replace("%CE%98", "Θ")
	            				.replace("%CE%99", "Ι")
	            				.replace("%CE%9A", "Κ")
	            				.replace("%CE%9B", "Λ")
	            				.replace("%CE%9C", "Μ")
	            				.replace("%CE%9D", "Ν")
	            				.replace("%CE%9E", "Ξ")
	            				.replace("%CE%9F", "Ο")
	            				.replace("%CE%A0", "Π")
	            				.replace("%CE%A0", "Π")
	            				.replace("%CE%A1", "Ρ")
	            				.replace("%CE%A3", "Σ")
	            				.replace("%CE%A4", "Τ")
	            				.replace("%CE%A5", "Υ")
	            				.replace("%CE%A6", "Φ")
	            				.replace("%CE%A7", "Χ")
	            				.replace("%CE%A8", "Ψ")
	            				.replace("%CE%A9", "Ω")
	            				.replace("%CE%AA", "Ϊ")
	            				.replace("%CE%AB", "Ϋ")
	            				.replace("%CE%AC", "ά")
	            				.replace("%CE%AD", "έ")
	            				.replace("%CE%AE", "ή")
	            				.replace("%CE%AF", "ί")
	            				.replace("%CE%B0", "ΰ")
	            				.replace("%CE%B1", "α")
	            				.replace("%CE%B2", "β")
	            				.replace("%CE%B3", "γ")
	            				.replace("%CE%B4", "δ")
	            				.replace("%CE%B5", "ε")
	            				.replace("%CE%B6", "ζ")
	            				.replace("%CE%B7", "η")
	            				.replace("%CE%B8", "θ")
	            				.replace("%CE%B9", "ι")
	            				.replace("%CE%BA", "κ")
	            				.replace("%CE%BB", "λ")
	            				.replace("%CE%BC", "μ")
	            				.replace("%CE%BD", "ν")
	            				.replace("%CE%BE", "ξ")
	            				.replace("%CE%BF", "ο")
	            				.replace("%CF%80", "π")
	            				.replace("%CF%81", "ρ")
	            				.replace("%CF%82", "ς")
	            				.replace("%CF%83", "σ")
	            				.replace("%CF%84", "τ")
	            				.replace("%CF%85", "υ")
	            				.replace("%CF%86", "φ")
	            				.replace("%CF%87", "χ")
	            				.replace("%CF%88", "ψ")
	            				.replace("%CF%89", "ω")
	            				.replace("%CF%8A", "ϊ")
	            				.replace("%CF%8B", "ϋ")
	            				.replace("%CF%8C", "ό")
	            				.replace("%CF%8D", "ύ")
	            				.replace("%CF%8E", "ώ")
	            				.replace("%CF%8F", "Ϗ")
	            				.replace("%CF%90", "ϐ")
	            				.replace("%CF%91", "ϑ")
	            				.replace("%CF%92", "ϒ")
	            				.replace("%CF%93", "ϓ")
	            				.replace("%CF%94", "ϔ")
	            				.replace("%CF%95", "ϕ")
	            				.replace("%CF%96", "ϖ")
	            				.replace("%CF%97", "ϗ")
	            				.replace("%CF%98", "Ϙ")
	            				.replace("%CF%99", "ϙ")
	            				.replace("%CF%9A", "Ϛ")
	            				.replace("%CF%9B", "ϛ")
	            				.replace("%CF%9C", "Ϝ")
	            				.replace("%CF%9D", "ϝ")
	            				.replace("%CF%9E", "Ϟ")
	            				.replace("%CF%9F", "ϟ")
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
	            				.replace("%D0%80", "Ѐ")
	            				.replace("%D0%81", "Ё")
	            				.replace("%D0%82", "Ђ")
	            				.replace("%D0%83", "Ѓ")
	            				.replace("%D0%84", "Є")
	            				.replace("%D0%85", "Ѕ")
	            				.replace("%D0%86", "І")
	            				.replace("%D0%87", "Ї")
	            				.replace("%D0%88", "Ј")
	            				.replace("%D0%89", "Љ")
	            				.replace("%D0%8A", "Њ")
	            				.replace("%D0%8B", "Ћ")
	            				.replace("%D0%8C", "Ќ")
	            				.replace("%D0%8D", "Ѝ")
	            				.replace("%D0%8E", "Ў")
	            				.replace("%D0%8F", "Џ")
	            				.replace("%D0%90", "А")
	            				.replace("%D0%91", "Б")
	            				.replace("%D0%92", "В")
	            				.replace("%D0%93", "Г")
	            				.replace("%D0%94", "Д")
	            				.replace("%D0%95", "Е")
	            				.replace("%D0%96", "Ж")
	            				.replace("%D0%97", "З")
	            				.replace("%D0%98", "И")
	            				.replace("%D0%99", "Й")
	            				.replace("%D0%9A", "К")
	            				.replace("%D0%9B", "Л")
	            				.replace("%D0%9C", "М")
	            				.replace("%D0%9D", "Н")
	            				.replace("%D0%9E", "О")
	            				.replace("%D0%9F", "П")
	            				.replace("%D0%A0", "Р")
	            				.replace("%D0%A1", "С")
	            				.replace("%D0%A2", "Т")
	            				.replace("%D0%A3", "У")
	            				.replace("%D0%A4", "Ф")
	            				.replace("%D0%A5", "Х")
	            				.replace("%D0%A6", "Ц")
	            				.replace("%D0%A7", "Ч")
	            				.replace("%D0%A8", "Ш")
	            				.replace("%D0%A9", "Щ")
	            				.replace("%D0%AA", "Ъ")
	            				.replace("%D0%AB", "Ы")
	            				.replace("%D0%AC", "Ь")
	            				.replace("%D0%AD", "Э")
	            				.replace("%D0%AE", "Ю")
	            				.replace("%D0%AF", "Я")
	            				.replace("%D0%B0", "а")
	            				.replace("%D0%B1", "б")
	            				.replace("%D0%B2", "в")
	            				.replace("%D0%B3", "г")
	            				.replace("%D0%B4", "д")
	            				.replace("%D0%B5", "е")
	            				.replace("%D0%B6", "ж")
	            				.replace("%D0%B7", "з")
	            				.replace("%D0%B8", "и")
	            				.replace("%D0%B9", "й")
	            				.replace("%D0%BA", "к")
	            				.replace("%D0%BB", "л")
	            				.replace("%D0%BC", "м")
	            				.replace("%D0%BD", "н")
	            				.replace("%D0%BE", "о")
	            				.replace("%D0%BF", "п")
	            				.replace("%D1%80", "р")
	            				.replace("%D1%81", "с")
	            				.replace("%D1%82", "т")
	            				.replace("%D1%83", "у")
	            				.replace("%D1%84", "ф")
	            				.replace("%D1%85", "х")
	            				.replace("%D1%86", "ц")
	            				.replace("%D1%87", "ч")
	            				.replace("%D1%88", "ш")
	            				.replace("%D1%89", "щ")
	            				.replace("%D1%8A", "ъ")
	            				.replace("%D1%8B", "ы")
	            				.replace("%D1%8C", "ь")
	            				.replace("%D1%8D", "э")
	            				.replace("%D1%8E", "ю")
	            				.replace("%D1%8F", "я")
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

	            		for(int j = split2.length-1; counter > 0; j--){
	            			//System.out.println(split2[i]);
	            			split2[j] = "";
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
	            	else{
	            		if(link.attr("href").isEmpty()){
	            			continue;
	            		}
	            		else{
	            			arr.add(link.attr("href"));
	            		}
	            	}
	            }
	        }
	    	return arr;
	    }
	
		public void crawlFiles(File[] files, Extractor ext,DB db, DBCollection table, String url) {
	        if (files != null) {
	           
	            for (int i = 0; i < files.length; i++) {
	            	File file = files[i];
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
	                    			JSONArray dataSet = ext.extract(file);
	                    			JSONObject metadata = ext.extractMeta(file);
	                    			
	                    			if(table.findOne(new BasicDBObject("hash", ext.SHA256Converter(url+"/"+file.getName()))) == null){
				                        synchronized(desk.lock){
				                        	ext.exportJson(file, file.getName(), url+"/"+file.getName(), dataSet, metadata, table, getLinks(url, metadata.get("Content-Encoding").toString() ,file));
				                        	ext.indexTerms(db, ext.SHA256Converter(url+"/"+file.getName()), file, desk.mapper);
				                        	System.out.println( Thread.currentThread().toString()+ ": " + desk.visitedAlready.size());
				                        }
			                        }
	                    		}
	                    		
	                    		
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
	        //Get the connection.
	        db = mongoClient.getDB("crawler");
	        
	        System.out.println("Connected to MongoDB!");
//	        db.getCollection("urlpages").drop();
//	        db.getCollection("index").drop();
//	        db.getCollection("pagerank").drop();
	         
	        Extractor ext = new Extractor();
	        File[] files = new File("C:/data/en").listFiles();
	        
	        DeskThreads desk = new DeskThreads(50, files, ext, "http://www.ctrlv.com/en");
	        desk.run();
	        desk.mapper.insertDB(db);

	        Ranking ranker = new Ranking(db);
	        ranker.link_analysis();
	    }

	}

