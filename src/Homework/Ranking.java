package Homework;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Ranking {
	// TF = total count of the word in document / total words in document
	// IDF = log total documents looking at / total # of documents containing the word
	// The word will be based on the search query
	// Need to crawl desktop
	DB database;
	DBCollection table = database.getCollection("urlpages");
	DBCollection index = database.getCollection("index");
	
	public Ranking(DB database) {
		this.database = database;
	}



	public void TFIDF ( String term ) {
		DBObject object = index.findOne(new BasicDBObject("word", term));
		
		
	}
	
	

}
