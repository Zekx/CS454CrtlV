package Homework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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

class Main{

    public static void main(String[] args) throws IOException {

        //Connects to the Mongo Database.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = null;
        DBCollection table = null;

        System.out.println("Establishing connection...");

        //Get the connection.
        db = mongoClient.getDB("crawler");
        table = db.getCollection("urlpages");

        System.out.println("Connected to MongoDB!");

        //Connects to a URL. Goes up to 3 levels.
        //Begins with one url, Tier: 0, totalSize 1.
        String u = null, d = null;
        Boolean e = false;

        try{
            for(int i = 0; i < args.length; i++){
                if(args[i].equals("-e")){
                    e = true;
                }
                if(args[i].equals("-d")){
                    d = args[i+1];
                }
                if(args[i].equals("-u")){
                    u = args[i+1];
                }
            }

            Crawler crawl = new Crawler();
            crawl.urlCrawler(u, Integer.parseInt(d), e, 0, 1, table);



            System.out.println("\nThe crawler has completed its run!");

            if(e){

                Extractor ext = new Extractor();
                Map<String, String> pages = crawl.getPageNames();

                Iterator it = pages.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry item = (Map.Entry) it.next();
                    System.out.println("Now extracting " + (String)item.getKey());

                    File file = new File("C:\\data\\htmls\\"+(String)item.getKey()+".html");

                    //ArrayList<String> set = ext.extract(file);

                    JSONArray dataSet;
                    dataSet = ext.extract(file);
                    //For extracting data information

//                    for(String s : set){
//
//                        System.out.println(s + " ");
//                        dataSet.add(s);
//                    }

                    JSONObject metadata = ext.extractMeta(file);

                    ext.exportJson(file, (String)item.getKey(),(String)item.getValue(), metadata, dataSet, table);

            }
            }

            mongoClient.close();
        }catch(Exception found){
            System.out.println("The inputted parameters were invalid.");
        }

    }

}
