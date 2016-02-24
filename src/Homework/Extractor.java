package Homework;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mchange.v1.util.ArrayUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.TeeContentHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by NIck on 2/6/2016.
 */
public class Extractor {
    private ArrayList<String> stopWords;
    private ArrayList<String> documentWords;
    private JSONArray data;
    private JSONObject meta;
    
    public Extractor(){
    	stopWords = new ArrayList<String>();
    	
    	File file = new File(".\\resources\\stopwords.txt");
    	try{
    		BufferedReader stream = new BufferedReader(new FileReader(file));
    		
    		String line = stream.readLine();

    	    while (line != null) {
    	    	System.out.println(line);
    	        stopWords.add(line);
    	        line = stream.readLine();
    	    }
    		
    	    stream.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    //This method extracts the specific document and outputs a set of string
    //Not sure if we should keep track of the count per word
    public JSONArray extract(File file)  throws IOException, SAXException, TikaException
    {
        //I heard wrapping the FileInputStream in BufferedInputStream is faster, idk if it actually is
        try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler(1000000);
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());

            data = new JSONArray();

            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            String[] split = bodyHandler.toString().replaceAll("\\s+"," ").split(" ");
            for(String s: split){
            	//System.out.print(s + " ");
            	
                //Tokenizing the strings
                if(!s.equals("") && !this.stopWords.contains(s)){
                	if(s.contains("-") || s.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")){
                    	String[] split2 = s.replaceAll("-", " ").split(" ");
                    	data.add(s.replaceAll("[+.^:,]","").toLowerCase());
                    	
                    	for(String t: split2){
                    		if(!t.replaceAll("[^a-zA-Z]+","").toLowerCase().equals(""))data.add(t.replaceAll("[^a-zA-Z]+","").toLowerCase());
                    	}
                    }
                    else{
                    	if(!s.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("")) data.add(s.replaceAll("[^a-zA-Z]+","").toLowerCase());
                    }
                }
            }

//            set.add(handler.toString().replaceAll("\\s+"," ")); //This should remove most of the white spaces
//            data.add(handler.toString().replaceAll("\\s+"," "));
//            System.out.println(handler.toString().replaceAll("\\s+"," "));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public JSONObject extractMeta(File file) throws IOException, SAXException, TikaException{
        try {

            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler(1000000);
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());

            meta = new JSONObject();
            //For extracting metadata information
            for(String s : metadata.names()){
               // System.out.println(s + " : " + metadata.get(s));
                meta.put(s, metadata.get(s));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return meta;
    }

    //This extracts the information to a JSON file
    public void exportJson(File file, String name, String url, JSONObject metadata, DBCollection table)
    {
        //
        BasicDBObject doc = new BasicDBObject()
                .append("name", name)
                .append("url", url)
                .append("hash", url.hashCode())
                .append("Document length", data.size())
                .append("metadata", metadata)
                .append("path", file.toString());

        table.insert(doc);
    }
    
    public void indexTerms(DB db, int urlHash, File file) throws InterruptedException{
    	DBCollection table = db.getCollection("urlpages");
    	DBCollection index = db.getCollection("index");
    	documentWords = new ArrayList<String>();
    	
    	//I heard wrapping the FileInputStream in BufferedInputStream is faster, idk if it actually is
        try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler(1000000);
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());
            
            int counter = 0;
            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            String[] split = bodyHandler.toString().replaceAll("\\s+"," ").split(" ");
            for(String s : split){
            	
            	if(s.contains("-") || s.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")){
                	String[] split2 = s.replaceAll("-", " ").split(" ");
                	documentWords.add(s.replaceAll("[^a-zA-Z-]+","").toLowerCase());
                	
                	for(String t: split2){
                		if(!t.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("")) documentWords.add(t.replaceAll("[^a-zA-Z]+","").toLowerCase());
                	}
                }
                else{
                	if(!s.replaceAll("[^a-zA-Z]+","").toLowerCase().equals("")) documentWords.add(s.replaceAll("[^a-zA-Z]+","").toLowerCase());
                }

            }
            
            for(String s : documentWords){  
  		
        		if(!this.stopWords.contains(s)){
        			if(index.findOne(new BasicDBObject("word",s.toString())) == null){
        				JSONArray doc = new JSONArray();
        				JSONObject innerDoc = new JSONObject();
        				
        				innerDoc.put("Frequency", 1);
        				innerDoc.put("Positions", counter);
        				innerDoc.put("docHash", urlHash);
        				doc.add(innerDoc);
        				
        				BasicDBObject entry = new BasicDBObject()
        					.append("word", s.toString())
        					.append("document", doc);
        				
        				index.insert(entry);
        			}
        			else{
        				DBObject entry = index.findOne(new BasicDBObject("word",s.toString()));
        				
        				BasicDBList doc = (BasicDBList) entry.get("document");
        				JSONObject innerDoc = new JSONObject();
        				Boolean docUpdate = false;
        				
        				for(Object docu: doc){
        					BasicDBObject item = (BasicDBObject) docu;
        					
        					if((int)item.get("docHash") == urlHash){     
        						int freq = Integer.parseInt(item.get("Frequency").toString()) + 1;
        						
            					innerDoc.put("Frequency", Integer.toString(freq));
                				innerDoc.put("Positions", item.get("Positions") + " " + counter);
                				innerDoc.put("docHash", urlHash);
            					
            					System.out.println(entry.get("word"));
            					doc.remove(item);
                				doc.add(innerDoc);
                				System.out.println(doc);
                				
                				BasicDBObject update = new BasicDBObject();
            					update.put("$set", new BasicDBObject("word", s.toString()));
            					update.put("$set", new BasicDBObject("document", doc));
                				
                				index.update(new BasicDBObject("word", s.toString()), update);
                				docUpdate = true;
            				}	       					
        				}
        				if(!docUpdate){
        					innerDoc.put("Frequency", 1);
            				innerDoc.put("Positions", counter);
            				innerDoc.put("docHash", urlHash);
        					
        					System.out.println(entry.get("word"));
            				doc.add(innerDoc);
            				System.out.println(doc);
            				
            				BasicDBObject update = new BasicDBObject();
        					update.put("$set", new BasicDBObject("word", s.toString()));
        					update.put("$set", new BasicDBObject("document", doc));
            				
            				index.update(new BasicDBObject("word", s.toString()), update);
        				}
        			}
        		}
        		
        		counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, TikaException
    {
//        //Connects to the Mongo Database.
//        MongoClient mongoClient = new MongoClient("localhost", 27017);
//        DB db = null;
//        DBCollection table = null;
//
//        System.out.println("Establishing connection...");
//
//        //Get the connection.
//        db = mongoClient.getDB("crawler");
//        table = db.getCollection("urlpages");
//
//        File file = new File ("C:\\data\\htmls\\99 Homes - Movies & TV on Google Play.html");
//        Extractor ext = new Extractor();


    }

}
