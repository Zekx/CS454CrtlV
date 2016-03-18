package Homework.Index;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Index {
	ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> indexMap;
	
	public Index(){
		indexMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
	}
	
	public void Tokenize(String[] split, CopyOnWriteArrayList<String> documentWords, ArrayList<String> stopWords){
		for(String s : split){
			if(!stopWords.contains(s)){
				if(s.contains("-")){
					documentWords.add(s.replaceAll("[^a-zA-Z-]+", "").toLowerCase());
					String[] split2 = s.split("-");
					
					for(String t: split2){
						if(!t.isEmpty()){
							documentWords.add(t.replaceAll("[^a-zA-Z]+", "").toLowerCase());
						}
					}
				}
				else{
					if(s.contains("'")){
						String[] split2 = s.split("'");
						
						for(String t: split2){
							if(t.length() > 2){
								documentWords.add(t.replaceAll("[^a-zA-Z]+", "").toLowerCase());
							}
						}
					}else{
						documentWords.add(s.replaceAll("[^a-zA-Z]+", "").toLowerCase());
					}
				}
			}	
		}
    }
	
	public synchronized void indexDoc(String urlHash, ArrayList<String> stopWords, File file){
    	CopyOnWriteArrayList<String> documentWords = new CopyOnWriteArrayList<String>();
		
		try{
            InputStream stream = new FileInputStream(file);
            BodyContentHandler bodyHandler = new BodyContentHandler(1000000);
            Metadata metadata = new Metadata();
            new HtmlParser().parse(stream, bodyHandler, metadata, new ParseContext());

            //System.out.println(bodyHandler.toString().replaceAll("\\s+"," "));
            String[] split = bodyHandler.toString().replaceAll("\\s+"," ").split(" ");
            this.Tokenize(split, documentWords, stopWords);
            Iterator<String> iter = documentWords.iterator();
            
            while(iter.hasNext()){  
            	String word = iter.next();
            	
            	if(!stopWords.contains(word)){
            		if(!stopWords.contains(word)){
                		if(!indexMap.containsKey(word)){
                    		ConcurrentHashMap<String, Integer> documents = new ConcurrentHashMap<String, Integer>();
                    		documents.put(urlHash, 1);
                    		
                    		indexMap.put(word, documents);
                    	}
                    	else{
                    		ConcurrentHashMap<String, Integer> documents = indexMap.get(word);
                    		if(!documents.containsKey(urlHash)){
                    			documents.put(urlHash, 1);
                    		}
                    		else{
                    			documents.put(urlHash, documents.get(urlHash)+1);
                    		}
                    		indexMap.put(word, documents);
                    	}
                	}
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void insertDB(DB db){
		DBCollection index = db.getCollection("index");
		if(indexMap.containsKey("")){
			indexMap.remove("");
		}
		
		Iterator iter = indexMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, ConcurrentHashMap<String, Integer>> word = (Map.Entry<String, ConcurrentHashMap<String, Integer>>) iter.next();
			String s = word.getKey();
			
			if(index.findOne(new BasicDBObject("word",s.toString())) == null){
				ConcurrentHashMap<String, Integer> documents = word.getValue();
				Iterator iter2 = documents.entrySet().iterator();
				
				JSONArray doc = new JSONArray();
				while(iter2.hasNext()){
					Map.Entry<String, Integer> oneDoc = (Map.Entry<String, Integer>)iter2.next();
					String docHash = oneDoc.getKey();
					Integer freq = oneDoc.getValue();
					
					JSONObject innerDoc = new JSONObject();
					innerDoc.put("Frequency", freq);
					innerDoc.put("docHash", docHash);
					doc.add(innerDoc);
					
				}
				BasicDBObject entry = new BasicDBObject()
	    					.append("word", s.toString())
	    					.append("document", doc);
	    				
	    				index.insert(entry);
			}
			else{
				DBObject entry = index.findOne(new BasicDBObject("word",s.toString()));
				
				BasicDBList doc = (BasicDBList) entry.get("document");
				Boolean docUpdate = false;
				
				CopyOnWriteArrayList<Object> arr = new CopyOnWriteArrayList<Object>();
				for(int i = 0; i < doc.size(); i++){
					arr.add(doc.get(i));
				}
				
				ConcurrentHashMap<String, Integer> documents = word.getValue();
				Iterator iter2 = documents.entrySet().iterator();
				
				while(iter2.hasNext()){
					Map.Entry<String, Integer> oneDoc = (Map.Entry<String, Integer>)iter2.next();
					String docHash = oneDoc.getKey();
					Integer freq = oneDoc.getValue();
					
					for(Object docu: arr){
    					BasicDBObject item = (BasicDBObject) docu;
    					
    					if( item.get("docHash").equals(docHash)){ 
    						JSONObject innerDoc = new JSONObject();
        					innerDoc.put("Frequency", freq);
            				innerDoc.put("docHash", docHash);
        					
        					doc.remove(item);
            				doc.add(innerDoc);
            				docUpdate = true;
        				}	       					
    				}
    				if(!docUpdate){
    					JSONObject innerDoc = new JSONObject();
    					innerDoc.put("Frequency", freq);
        				innerDoc.put("docHash", docHash);
    					
        				doc.add(innerDoc);
    				}
    				
    				BasicDBObject update = new BasicDBObject();
					update.put("$set", new BasicDBObject("word", s.toString()));
					update.put("$set", new BasicDBObject("document", doc));
    				index.update(new BasicDBObject("word", s.toString()), update);
				}
			}
        }
	}
}
