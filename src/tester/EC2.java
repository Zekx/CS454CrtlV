package tester;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class EC2 {
	public static void main(String[] args){
		MongoClient mongoClient = new MongoClient("ec2-52-36-142-197.us-west-2.compute.amazonaws.com", 27017);
        DB db = null;

        System.out.println("Establishing connection...");

        //Get the connection.
        db = mongoClient.getDB("crawler");
        DBCollection table = db.getCollection("urlpages");
	}
}
