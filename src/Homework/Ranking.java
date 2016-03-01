package Homework;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Ranking {
	// TF = total count of the word in document / total words in document
	// IDF = log total documents looking at / total # of documents containing the word
	// The word will be based on the search query
	// Need to crawl desktop
	public void link_analysis()
	{
		
	}
	private DB database;
	public DBCollection table;
	public DBCollection index;
	public DBCollection freqNum;
	
	public Ranking(DB database) {
		this.database = database;
		
		table = database.getCollection("urlpages");
		index = database.getCollection("index");
		freqNum = database.getCollection("ranking");
	}

	public void TFIDF ( String term ) {
		System.out.println("Hello!");
		
		DBObject object = index.findOne(new BasicDBObject("word", term));
		// Will go call the TF method to get the tf number for each document
		BasicDBList docList = (BasicDBList) object.get("document"); // JSON Object now
		
		double tfNum,tfidfNum;
		double idfNum = IDF(docList.size());
		for (Object docu : docList) {
			BasicDBObject obj = (BasicDBObject) docu;
			int wordCount = Integer.parseInt(obj.get("Frequency").toString());
			int docSize = Integer.parseInt(table.findOne(new BasicDBObject("hash",
					obj.get("docHash"))).get("Document length").toString());
			tfNum = TF(wordCount, docSize);
			tfidfNum = tfNum * idfNum;
			
			BasicDBObject tfRankObj = new BasicDBObject()
					.append("docHash", obj.get("docHash"))
					.append("TFIDF", tfidfNum);
			freqNum.insert(tfRankObj);
		}
		
	}
	
	public double TF (int wordCount, int docSize) {
		double wc = (double) wordCount;
		double ds = (double) docSize;
		return wc/ds;
	}
	
	public double IDF (int listSize) {
		double docCount = table.count();
		double logDoc = docCount / (double) listSize;
		return Math.log(logDoc);
	}
	
	public static void main(String[] args){
		//Connects to the Mongo Database.
        MongoClient mongoClient = new MongoClient("localhost", 27017);

        System.out.println("Establishing connection...");

        //Get the connection.
        DB db = mongoClient.getDB("crawler");
        DBCollection table = db.getCollection("urlpages");

        System.out.println("Connected to MongoDB!");
        
        Ranking ranker = new Ranking(db);
        ranker.TFIDF("google");
		
//        List<DBObject> result = new ArrayList<DBObject>();
//		DBObject sample = new BasicDBObject();
//		DBObject removeID = new BasicDBObject("_id", 0);
//		
//		DBCursor cursor = table.find(sample, removeID);
//		
//		while(cursor.hasNext())
//		{
//			result.add(cursor.next());
//		}
	}
}
