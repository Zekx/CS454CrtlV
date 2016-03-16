package Homework.Dump;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class rank_dump {
	public static List<DBObject> getList(DBCollection table)
	{
		List<DBObject> result = new ArrayList<DBObject>();
		DBObject sample = new BasicDBObject();
		DBObject removeID = new BasicDBObject("_id", 0);
		
		DBCursor cursor = table.find(sample, removeID);
		
		while(cursor.hasNext())
		{
			result.add(cursor.next());
		}
		
		
		return result;
	}
	
    public void rankFile(List<DBObject> result, File file) throws IOException {
        //List<DBObject> result = table.find().toArray();
                
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer2 = mapper.defaultPrettyPrintingWriter();

        for(DBObject object : result)
        {
        	writer.write(mapper.defaultPrettyPrintingWriter().writeValueAsString(object)+ ",");
        	System.out.println(object.toString());
        	
        	
        }
        writer.flush();
        writer.close();
    }
    
    public static void main(String[] args) throws IOException
    {
    	 MongoClient mongoClient = new MongoClient("localhost", 27017);
         DB db = null;
         DBCollection table = null;
         DBCollection table2 = null;

         System.out.println("Establishing connection...");

         //Get the connection.
         db = mongoClient.getDB("crawler");
         table = db.getCollection("ranking");
         table2 = db.getCollection("pagerank");
         
         if(!db.collectionExists("pagerank"))
         {
        	 db.createCollection("pagerank", null);
         }
         
         rank_dump test = new rank_dump();
         System.out.println("Connected to MongoDB!");

         File file2 = new File("C:\\data\\page_rank.json");
         List<DBObject> list2 = getList(table2);
         test.rankFile(list2, file2);
    }
}
