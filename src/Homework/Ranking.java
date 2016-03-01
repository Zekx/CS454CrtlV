package Homework;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
	DB database;
	DBCollection table = database.getCollection("urlpages");
	DBCollection index = database.getCollection("index");
	DBCollection freqNum = database.getCollection("ranking");
	
	public Ranking(DB database) {
		this.database = database;
	}



	public void TFIDF ( String term ) {
		DBObject object = index.findOne(new BasicDBObject("word", term));
		// Will go call the TF method to get the tf number for each document
		BasicDBList docList = (BasicDBList) object.get("Document"); // JSON Object now
		BasicDBList freqList = (BasicDBList) docList.get("Frequency");
		double tfNum,tfidfNum;
		double idfNum = IDF(docList.size());
		for (int i = 0; i < docList.size(); i ++ ) {
			JSONObject currObj = (JSONObject) docList.get(i);
			int docSize = (int) table.findOne(new BasicDBObject("hash", currObj.get("docHash"))).get("Document length");
			tfNum = TF((int) currObj.get("Frequency"), docSize);
			tfidfNum = tfNum * idfNum;
		}
		// Call IDF method to get the IDF number
		
		
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
}
