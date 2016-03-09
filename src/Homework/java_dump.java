package Homework;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class java_dump
{
    public void dumpFile(DBCollection table) throws FileNotFoundException {
        JsonArray jsonList = null;
        DBCursor list = table.find();
        while (list.hasNext())
        {
            DBObject object = list.next();
            jsonList = Json.createArrayBuilder().add(Json.createObjectBuilder()
                    .add("name", (String)object.get("name"))
                    .add("url", (String)object.get("url"))
                    .add("metadata", (String)object.get("metadata"))
                    .add("data", (String)object.get("data"))
                    .add("path", (String)object.get("path")))
                    .build();
        }
            JsonWriter writer = Json.createWriter(new FileOutputStream("C:\\data\\dump_file.json"));
            writer.write(jsonList);
            writer.close();
    }
    
    public static void main(String[] args){
    	MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB db = null;
        DBCollection table = null;

        System.out.println("Establishing connection...");

        //Get the connection.
        db = mongoClient.getDB("crawler");
        table = db.getCollection("urlpages");
    	
    	java_dump dump = new java_dump();
    	try {
			dump.dumpFile(table);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
