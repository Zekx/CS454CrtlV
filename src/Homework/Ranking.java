package Homework;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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
		BasicDBList docList = (BasicDBList) object.get("Document");
		double idfNum = IDF(docList.size());
		// Call IDF method to get the IDF number
		
		
	}
	
	public double IDF (int listSize) {
		double docCount = table.count();
		return docCount / (double)listSize;
	}
}
