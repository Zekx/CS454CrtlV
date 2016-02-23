package Homework;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    public void exportJson(File file, String name, String url, JSONObject metadata, JSONArray data, DBCollection table)
    {
        //
        BasicDBObject doc = new BasicDBObject()
                .append("name", name)
                .append("url", url)
                .append("hash", url.hashCode())
                .append("Document length", data.size())
                .append("metadata", metadata)
                .append("data", data)
                .append("path", file.toString());

        table.insert(doc);
    }
    
    public int getFrequency(BasicDBList list, Object w){
    	int total = 0;
    	
    	for(Object str: list){
    		if(str.equals(w)){
    			total++;
    		}
    	}
    	
    	return total;
    }
    
    public JSONArray getPositions(BasicDBList list, Object w){
    	JSONArray arr = new JSONArray();
    	Integer index = 0;
    	
    	for(Object str: list){
    		if(str.equals(w)){
    			arr.add(index);
    		}
    		index++;
    	}
    	
    	return arr;
    }
    
    public void index(DB db, int urlHash) throws InterruptedException{
    	DBCollection table = db.getCollection("urlpages");
    	DBCollection index = db.getCollection("index");
    	
    	BasicDBObject query = new BasicDBObject("hash", urlHash);
    	DBObject document = table.findOne(query);
    	
    	//Retrieves the list of words found in a document.
    	BasicDBList words = (BasicDBList) document.get("data");
    	for(Object w: words){
    		int occurrence = this.getFrequency(words, w);
    		JSONArray positions = this.getPositions(words, w);
    		//System.out.println(w);
    		
    		if(!this.stopWords.contains(w)){
    			if(index.findOne(new BasicDBObject("word",w.toString())) == null){
    				JSONObject doc = new JSONObject();
    				JSONObject innerDoc = new JSONObject();
    				
    				innerDoc.put("Frequency", occurrence);
    				innerDoc.put("Positions", positions);
    				doc.put(Integer.toString(urlHash), innerDoc);
    				
    				BasicDBObject entry = new BasicDBObject()
    					.append("word", w.toString())
    					.append("document", doc);
    				
    				index.insert(entry);
    			}
    			else{
    				DBObject entry = index.findOne(new BasicDBObject("word",w.toString()));
    				
    				BasicDBObject doc = (BasicDBObject) entry.get("document");
    				JSONObject innerDoc = new JSONObject();
    				innerDoc.put("Frequency", occurrence);
    				innerDoc.put("Positions", positions);
    				
    				if(!doc.containsField(Integer.toString(urlHash))){
    					System.out.println(entry.get("word"));
        				doc.append(Integer.toString(urlHash), innerDoc);
        				System.out.println(doc);
        				
        				BasicDBObject update = new BasicDBObject();
    					update.put("$set", new BasicDBObject("word", w.toString()));
    					update.put("$set", new BasicDBObject("document", doc));
        				
        				index.update(new BasicDBObject("word", w.toString()), update);
    				}
    			}
    		}
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
