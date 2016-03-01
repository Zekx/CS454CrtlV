package Homework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
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
            
            final long startTime = System.currentTimeMillis();
            WebThreads web = new WebThreads(20, u, Integer.parseInt(d), e);
            web.run();

            System.out.println("\nThe crawler has completed its run!");
            final long endTime = System.currentTimeMillis();

            System.out.println("Total execution time: " + (endTime - startTime));
            
            mongoClient.close();
        }catch(Exception found){
            System.out.println("The inputted parameters were invalid.");
        }

    }

}
