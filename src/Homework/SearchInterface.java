package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class SearchInterface {


	
	public static void run(String term, DB db) throws IOException
	{
		BasicDBList result = new BasicDBList();
		
		DBCollection index = db.getCollection("index");
		DBObject object = index.findOne(new BasicDBObject("word", term));
		BasicDBList docList = (BasicDBList) object.get("document"); 
		
		List<BasicDBObject> indexArr = new ArrayList<BasicDBObject>();
		List<BasicDBObject> regArr = new ArrayList<BasicDBObject>();
		
		for(int i = 0; i < docList.size(); i++){
			indexArr.add((BasicDBObject) docList.get(i));
		}
		
		DBCollection urlpages = db.getCollection("pagerank");
		for(int i = 0; i < indexArr.size(); i++)
		{
			regArr.add((BasicDBObject) urlpages.findOne(new BasicDBObject("Hash", indexArr.get(i).get("docHash"))));
		}
		
		writeFile(terribleSort(indexArr, regArr, "wm" ), "search_weighted_mean");
		writeFile(terribleSort(indexArr, regArr, "hm" ), "search_harmonic_mean");
	}
	
	public static List<BasicDBObject> terribleSort(List<BasicDBObject> indexArr, List<BasicDBObject> regArr, String calcType)
	{
		List<BasicDBObject> finalList = regArr;
		List<BasicDBObject> cloneIndex = indexArr;
		
		for(int i = 1; i < finalList.size(); i ++)
		{
			BasicDBObject temp1 = finalList.get(i);
			BasicDBObject temp2 = cloneIndex.get(i);
			
			int j;
			for(j = i - 1; j >= 0 && 
					getCalc(temp1.getDouble("PageRank Value"), temp2.getDouble("tfidf"), calcType) < 
					getCalc(finalList.get(j).getDouble("PageRank Value"), cloneIndex.get(j).getDouble("tfidf"), calcType); j--)
			{
				finalList.set(j + 1, finalList.get(j));
				cloneIndex.set(j + 1, cloneIndex.get(j));
			}
			finalList.set(j + 1, temp1);
			cloneIndex.set(j+1, temp2);
		}
		
		return finalList;
	}
	
	public static double getCalc(double pagerank, double tfidfRank, String calcType)
	{
		double result = 0;
		if(calcType.equals("wm"))
		{
			result = ( 0.6 * tfidfRank ) + (0.4 * pagerank);
		}
		if(calcType.equals("hm"))
		{
			result = 2.0 / (  ( 1.0/pagerank ) + ( 1.0/tfidfRank ) );
		}
		return result;
	}
	
		
	public static void writeFile(List<BasicDBObject> result, String type) throws IOException
	{
        File file = new File("C:\\data\\" + type + ".json");
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer2 = mapper.defaultPrettyPrintingWriter();

        for(BasicDBObject object : result)
        {
        	writer.write(mapper.defaultPrettyPrintingWriter().writeValueAsString(object)+ ",");
        	System.out.println(object.toString());
        	
        	
        }
        writer.flush();
        writer.close();
    }

	
	public static void main(String[] args) throws IOException
	{
		String term = JOptionPane.showInputDialog("Welcome to our knock-off search engine! \nPlease enter a term to search:");
		while(term == null || term.isEmpty())
		{
			term = JOptionPane.showInputDialog("You didn't input anything! \nPlease enter a term to search: ");
		}
        MongoClient mongoClient = new MongoClient("localhost", 27017);

        System.out.println("Establishing connection...");

        //Get the connection.
        DB db = mongoClient.getDB("crawler");
        DBCollection table = db.getCollection("urlpages");

        System.out.println("Connected to MongoDB!");
        db.getCollection("pagerank").drop();
		
		Ranking ranker = new Ranking(db);
		ranker.link_analysis();
		ranker.TFIDF(term);
		
		run(term, db);
		
		
		JOptionPane.showMessageDialog(null, "Search is completed! \nPlease refer to 'C:\\data\\search_results.json' for the result.");
	}
}
