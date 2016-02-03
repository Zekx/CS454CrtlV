package HW1;

import org.json.simple.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Crawler {
	private List<String> goingToVisit = new LinkedList<String>();
	private Set<String> visitedAlready = new HashSet<String>();
	
	public void urlCrawler(String url, int level, int levelSize, DBCollection db){
		while(level <= 3){
			System.out.println(url + " Tier:" + level + " current Tier Size:" + levelSize + " Current Total Size:" + this.goingToVisit.size());

			try{
				if(!url.replace(" ", "").isEmpty()){
					Connection connection = Jsoup.connect(url);
					Connection.Response resp = Jsoup.connect(url).timeout(100*1000).ignoreHttpErrors(false).followRedirects(true).execute();
					Document doc = null;

					//Checks for a 200 code.
					if(resp.statusCode() == 200){
						doc = connection.get();
					}

					if(doc != null){
						Elements allLinks = doc.select("a[href]");
						for(Element link : allLinks){
							if(link != null){
								if(!this.visitedAlready.contains(link.attr("abs:href"))){
									this.goingToVisit.add(link.attr("abs:href"));
								}
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			//This page has fully been visited.
			this.visitedAlready.add(url);

			//Goes to the next linked websites. The higher the number, the deeper the height.
			url = this.goingToVisit.remove(0);
			levelSize = levelSize - 1;

			//Checks to see if the next tier is coming based on the current tier's total queue list size.
			if(levelSize == 0){
				level = level + 1;
				levelSize = this.goingToVisit.size();
			}

			//If the following url has already been visited, then skip it.
			while(this.visitedAlready.contains(url)){
				url = this.goingToVisit.remove(0);
				levelSize = levelSize - 1;
				if(levelSize <= 0){
					levelSize = this.goingToVisit.size();
					level = level + 1;
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws IOException{
		
		//Connects to the Mongo Database.
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = null;
		DBCollection table = null;
		
		System.out.println("Establishing connection...");
		
		//Get the connection.
		db = mongoClient.getDB("crawler");
		table = db.getCollection("urlpages");
		
		System.out.println("Connected to MongoDB!");
		
		//Connects to a URL. Goes up to 3 levels.
		//Begins with one url, Tier: 0, totalSize 1.
		String url = "http://www.google.com/";
		Crawler crawl = new Crawler();
		crawl.urlCrawler(url, 0, 1, table);
		
		mongoClient.close();
		
	}
}
