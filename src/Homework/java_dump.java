package Homework;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class java_dump
{
    public void dumpFile(DBCollection table) throws IOException {
        List<DBObject> result = table.find().toArray();
        File file = new File("C:\\data\\dump_file.json");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        FileOutputStream fos = new FileOutputStream("C:\\data\\dump_file.json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer2 = mapper.defaultPrettyPrintingWriter();

        for(DBObject object : result)
        {
        	writer.write(mapper.defaultPrettyPrintingWriter().writeValueAsString(object));
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

         System.out.println("Establishing connection...");

         //Get the connection.
         db = mongoClient.getDB("crawler");
         table = db.getCollection("urlpages");
         
         java_dump test = new java_dump();
         System.out.println("Connected to MongoDB!");
         test.dumpFile(table);

    }
}
