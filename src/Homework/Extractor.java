package Homework;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import com.mongodb.MongoClient;
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
    private ArrayList<String> set;
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
            int docSize = split.length;
            for(String s: split){
                //Tokenizing the strings
                if(s.contains("-")){
                	String[] split2 = s.split("-");
                	data.add(s.replaceAll("[+.^:,]",""));
                	
                	for(String t: split2){
                		data.add(t.replaceAll("[+.^:,]",""));
                	}
                }
                else{
                	data.add(s.replaceAll("[+.^:,]",""));
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
                System.out.println(s + " : " + metadata.get(s));
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
                .append("Document length", data.size())
                .append("metadata", metadata)
                .append("data", data)
                .append("path", file.toString());

        table.insert(doc);
    }
    
    public void index(DB db){
    	DBCollection index = db.getCollection("index");
    	
    	
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
