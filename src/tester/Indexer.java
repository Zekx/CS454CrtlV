package tester;

import java.io.File;

import org.json.simple.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

//General notes for developing a Skeleton Search Engine.
//Use JSoup to extract data from html pages.

public class Indexer {
	
	public static void crawlFiles(File[] files, DBCollection coll) {
	    if(files != null){
	    	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd E hh:mm:ss", Locale.ENGLISH);
	    	for (File file : files) {
	    		Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
	    		
	    		try{
	    			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
	    			String fileName = file.getName();
	    			URL url = file.toURI().toURL();
	    			
	    			Date creationTime = format1.parse(attr.creationTime().toString().replace("Z", "").replace("T", " "));
	    			String newcreationTime = format2.format(creationTime);
	    			
	    			Date lastAccessTime = format1.parse(attr.lastAccessTime().toString().replace("Z", "").replace("T", " "));
	    			String newlastAccessTime = format2.format(lastAccessTime);
	    			
	    			Date lastModifiedTime = format1.parse(attr.lastModifiedTime().toString().replace("Z", "").replace("T", " "));
	    			String newlastModifiedTime = format2.format(lastModifiedTime);
	    			
	    			if (file.isDirectory()) {
			            crawlFiles(file.listFiles(), coll); // Calls same method again.
			            
			            BasicDBObject doc = new BasicDBObject()
			            		.append("name", fileName)
			            		.append("url", url)
			            		.append("creationTime", newcreationTime)
			            		.append("lastAccessTime", newlastAccessTime)
			            		.append("lastModifiedTime", newlastModifiedTime);
			            	
			            coll.insert(doc);
			        } else {
			        	BasicDBObject doc = new BasicDBObject()
			            		.append("name", fileName)
			            		.append("url", url)
			            		.append("creationTime", newcreationTime)
			            		.append("lastAccessTime", newlastAccessTime)
			            		.append("lastModifiedTime", newlastModifiedTime);
			            	
			            coll.insert(doc);
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
		crawlFiles(files, table);
		
	}
	
}
