package Homework;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
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

	
	
	public Set<String> convertToSet(BasicDBList bdba)
	{
		Set<String> set = new HashSet<String>();
		for(Object url : bdba)
		{
			set.add((String) url);
		}
		return set;
	}
	
	public JSONArray convertToJSONArray(Set<String> set)
	{
		JSONArray arr = new JSONArray();
		for (String s: set)
		{
			arr.add(s);
		}
		return arr;
	}
	
	public List<String> convertToArrayList(BasicDBList bdba)
	{
		List<String> returnURL = new ArrayList<String>();
		for(Object url : bdba)
		{
			returnURL.add((String) url);
		}
		return returnURL;
	}
	
	public void link_analysis()
	{	
		Hashtable<String, Object> htOut = new Hashtable<String, Object>();
		Hashtable<String, Object> htIn = new Hashtable<String, Object>();
		
		List<String> urlList = new ArrayList<String>();
		
		DBObject sample = new BasicDBObject();
		DBObject removeID = new BasicDBObject("_id", 0);
		
		DBCursor cursor = table.find(sample, removeID);
		
		while(cursor.hasNext())
		{
			DBObject temp = cursor.next();
			List<String> tempArr = convertToArrayList((BasicDBList) temp.get("links"));

			urlList.add(temp.get("url").toString());
			
			//adding to htOut ( Table of pages that link out )
			htOut.put(temp.get("url").toString(), convertToSet((BasicDBList) temp.get("links")));
			
			//adding to htIn ( Table of pages that is being linked )
			if(tempArr.size() > 0 )
			{
				for(int i = 0; i < tempArr.size(); i ++)
				{
					
					if(!htIn.containsKey(temp.get("url").toString()))
					{
						Set<String> tempSet = new HashSet<String>();
						tempSet.add(tempArr.get(i));
						htIn.put(temp.get("url").toString(), tempSet);
					}
					else
					{
						Set<String> tempSet = (Set<String>) htIn.get(temp.get("url"));
						tempSet.add(tempArr.get(i));
						htIn.put(temp.get("url").toString(), tempSet);
					}
				}
			}
			else
			{
				if(!htIn.containsKey(temp.get("url").toString()))
				{
					Set<String> tempSet = new HashSet<String>();
					htIn.put(temp.get("url").toString(), tempSet);
			
				}

			}
			
		}
		//Calculation
		Hashtable<String, Double> valueNew = new Hashtable<String, Double>();

		
		//This is setting value for the first
		double normal = 1.0/urlList.size();
		System.out.println(normal);
		
		for(int j = 0; j < urlList.size(); j ++)
		{
			valueNew.put(urlList.get(j), normal);
		}
		
		Hashtable<String, Double> valueOld = valueNew;
		
		//This is for the next 2 iterations
		int counter = 1;
		while(counter < 2)
		{
			for(int k = 0; k < urlList.size(); k ++)
			{
				Set<String> linked = (Set<String>) htIn.get(urlList.get(k));
				double sum = 0;
				for(String s : linked)
				{
					if(!s.equals(urlList.get(k))) //If the page is not linking itself
					{
						Set<String> size = (Set<String>) htOut.get(urlList.get(k));
						sum =+ (double) valueOld.get(urlList.get(k))/size.size();
					}
				}
				valueNew.put(urlList.get(k), sum);
			}
			valueOld = valueNew;
			
			counter++;
		}
		
		DBCollection pagerank = database.getCollection("pagerank");
		
		
		for(int x = 0; x < urlList.size(); x++)
		{
			BasicDBObject object = new BasicDBObject()
					.append("url", urlList.get(x))
					.append("Links out", convertToJSONArray((Set<String>) htOut.get(urlList.get(x))).size())
					.append("Link in", convertToJSONArray((Set<String>) htIn.get(urlList.get(x))).size())
					.append("PageRank Value", valueNew.get(urlList.get(x))); 
			pagerank.insert(object);
		}
		

        System.out.print("Finished!");
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
        ranker.link_analysis();
		
        
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
