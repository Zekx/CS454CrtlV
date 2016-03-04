package Homework;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Crawler implements Runnable{
	WebThreads web;
    
    public Crawler(WebThreads wb){
    	this.web = wb;
    }
    
    public List<String> readDoc(String url, Extractor ext, List<String> incoming, DB db, DBCollection table){
    	try{
    		if (!url.replace(" ", "").isEmpty()) {
                Connection connection = Jsoup.connect(url);
                Connection.Response resp = Jsoup.connect(url).timeout(5 * 1000).ignoreHttpErrors(true).followRedirects(true).execute();
                Document doc = null;
                
                incoming = new LinkedList<String>();
                //Checks for a 200 code.
                if (resp.statusCode() == 200) {
                    doc = connection.get();
                    
                    if (doc != null) {
                        	Elements allLinks = doc.select("a[href]");
                            for (Element link : allLinks) {
                                if (link != null) {
                                    synchronized(web.lock){
                                    	if (!web.visitedAlready.contains(link.attr("abs:href"))) {
                                        	incoming.add(link.attr("abs:href"));
                                    }
                                 }
                             }
                        }
                    }
                    synchronized(web.lock){
                    	web.visitedAlready.add(url);
                    }
                    
                    String htmlTitle = connection.maxBodySize(Integer.MAX_VALUE).get().title();
                	
                    File newHtmlFile = new File("C:/data/htmls/"+url.hashCode()+".html");
                    FileUtils.writeStringToFile(newHtmlFile, resp.body());
                    
                    //If -e is entered, then begin extraction here.
                    if(web.extract){
                    	File file = new File("C:\\data\\htmls\\"+url.hashCode()+".html");
                    	
                    	JSONArray dataSet = ext.extract(file);

                        JSONObject metadata = ext.extractMeta(file);
                        
                        ext.exportJson(file, htmlTitle, url, dataSet, metadata, table, ext.getLinks(url, metadata.get("Content-Encoding").toString() ,file));
                        ext.indexTerms(db, url.hashCode(), file);
                    }
                    
                    //System.out.println("ELEMENTS WITH IMG " + doc.getElementsByAttribute("src"));
                    //Give images the same name as the html. If exisiting image has the same name, add a number to the end.
                    // eg htmltitle-imgname-someNum.
                    String baseUrl = url.substring(0, url.indexOf("/", 7));
                    Elements imgs = doc.getElementsByTag("img");
                    for(int i = 0; i < imgs.size(); i++) {
                    	String src = imgs.get(i).attributes().get("src");
                    	if (!src.startsWith("http")) {
                    	downloadImage(baseUrl, src);
                    	} else {
                    		if (src.contains(baseUrl)) {
                    			downloadImage(baseUrl, src);
                    		}
                    	}
                    	
                    }
                }
            }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return incoming;
    }
    
    @Override
    public void run() {
    	Extractor ext = new Extractor();
    	List<String> incomingURL = new LinkedList<String>();
    	
    	//Connects to the Mongo Database.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = null;
        DBCollection table = null;

        System.out.println("Establishing connection...");

        //Get the connection.
        db = mongoClient.getDB("crawler");
        table = db.getCollection("urlpages");

        System.out.println("Connected to MongoDB!");
	        while (web.level <= web.height) {
	        	String url = web.goingToVisit.poll();
	
	            try {
	            	if(url == null){
	            		Thread.yield();
	            		continue;
	            	}
	                if(!web.visitedAlready.contains(url)){
	                	System.out.println(url + " Tier:" + web.level + " current Tier Size:" + web.levelSize + " Current Total Size:" + web.goingToVisit.size());
	                	incomingURL = this.readDoc(url, ext, incomingURL, db, table);
	                }
	            } catch (Exception e) {
	                System.out.println("\n following url page: " + url + " was unable to be read...\n");
	                
	                e.printStackTrace();
	            }
	            synchronized(web.lock){
	            	for(String str: incomingURL){
	            		if(!web.visitedAlready.contains(str)){
	            			web.goingToVisit.add(str);
	            		}
	            	}
	            	
	            	//This page has fully been visited.
	            	web.levelSize = web.levelSize - 1;
	
	                //Checks to see if the next tier is coming based on the current tier's total queue list size.
	                if (web.levelSize == 0) {
	                	web.level = web.level + 1;
	                	web.levelSize = web.goingToVisit.size();
	                }
	         }
        }
    }
    
    
    // Credits to http://www.compiletimeerror.com/2013/08/java-downloadextract-all-images-from.html#.Vr6KyObpxfY 
    // for the downloadImage method
    private static void downloadImage(String url, String imgSrc) throws IOException {
        BufferedImage image = null;
        try {
            if (!(imgSrc.startsWith("http"))) {
            	if(imgSrc.startsWith("/")) {
                    url = url + imgSrc;            		
            	} else {
            		url = url + "/" + imgSrc;
            	}

            } else {
                url = imgSrc;
            }
            imgSrc = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
            String imageFormat = null;
            imageFormat = imgSrc.substring(imgSrc.lastIndexOf(".") + 1);
            String imagename = imgSrc.substring(0, imgSrc.lastIndexOf("."));
            String imgPath = null;
            imgPath = "C:/data/images/" + imagename.hashCode() + "." + imageFormat + "";
            URL imageUrl = new URL(url);
            image = ImageIO.read(imageUrl);
            if (image != null) {
                File file = new File(imgPath);
                ImageIO.write(image, imageFormat, file);
            }
        } catch (Exception ex) {
            
        }

    }
}
