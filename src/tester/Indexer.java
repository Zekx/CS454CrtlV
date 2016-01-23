package tester;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

//General notes for developing a Skeleton Search Engine.
//Use JSoup to extract data from html pages.

public class Indexer {
	
	public static void crawlFiles(File[] files) {
	    if(files != null){
	    	for (File file : files) {
	    		Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
	    		
	    		try{
	    			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
	    			String fileName = file.getName();
	    			URL url = file.toURI().toURL();
	    			String creationTime = (attr.creationTime()).toString();
	    			String lastAccessTime = (attr.lastAccessTime()).toString();
	    			String lastModifiedTime = (attr.lastModifiedTime()).toString();
	    			
	    			if (file.isDirectory()) {
	    				System.out.println(url);
			            crawlFiles(file.listFiles()); // Calls same method again.
			        } else {
			        	System.out.println(url);
			        }
	    			
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
		    }
	    }
	}

	public static void main(String[] args) throws UnknownHostException{
		MongoClient mongoClient = new MongoClient();
		DB db = null;
		DBCollection table = null;
		
		//Establish connection to MongoDB.
		mongoClient = new MongoClient("localhost", 27017);
		
		System.out.println("Establishing connection...");
		
		//Get the connection.
		db = mongoClient.getDB("index");
		table = db.getCollection("url");
		
		System.out.println("Connected to MongoDB!");
		
		File[] files = new File("C:/").listFiles();
		crawlFiles(files);
		
	}
	
}
