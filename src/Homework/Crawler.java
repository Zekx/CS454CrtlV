package Homework;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Crawler {
    private List<String> goingToVisit = new LinkedList<String>();
    private Set<String> visitedAlready = new HashSet<String>();
    

    public void urlCrawler(String url, int height, Boolean extract, int level, int levelSize, DB database, DBCollection table) {
    	Indexer index = new Indexer();
    	Extractor ext = new Extractor();
    	
        while (level <= height) {
            System.out.println(url + " Tier:" + level + " current Tier Size:" + levelSize + " Current Total Size:" + this.goingToVisit.size());

            try {
                if (!url.replace(" ", "").isEmpty()) {
                    Connection connection = Jsoup.connect(url);
                    Connection.Response resp = Jsoup.connect(url).timeout(5 * 1000).ignoreHttpErrors(true).followRedirects(true).execute();
                    Document doc = null;
                    

                    //Checks for a 200 code.
                    if (resp.statusCode() == 200) {
                        doc = connection.get();
                        
                        if (doc != null) {
                            Elements allLinks = doc.select("a[href]");
                            for (Element link : allLinks) {
                                if (link != null) {
                                    if (!this.visitedAlready.contains(link.attr("abs:href"))) {
                                        this.goingToVisit.add(link.attr("abs:href"));
                                    }
                                }
                            }
                        }
                        
                        String htmlTitle = connection.maxBodySize(Integer.MAX_VALUE).get().title();
                    	
                        File newHtmlFile = new File("C:/data/htmls/"+url.hashCode()+".html");
                        FileUtils.writeStringToFile(newHtmlFile, resp.body());
                        
                        //If -e is entered, then begin extraction here.
                        if(extract){
                        	File file = new File("C:\\data\\htmls\\"+url.hashCode()+".html");
                        	
                        	JSONArray dataSet;

                            JSONObject metadata = ext.extractMeta(file);

                            ext.exportJson(file, htmlTitle, url, metadata, table);
                            //Indexer.run(file.toString().replace("\\", "/"));
                            ext.indexTerms(database, url.hashCode(), file);
                        }
                        
                        //System.out.println("ELEMENTS WITH IMG " + doc.getElementsByAttribute("src"));
                        //Give images the same name as the html. If exisiting image has the same name, add a number to the end.
                        // eg htmltitle-imgname-someNum.
                        String baseUrl = url.substring(0, url.indexOf("/", 7));
                        Elements imgs = doc.getElementsByTag("img");
                        for(int i = 0; i < imgs.size(); i++) {
                        	String src = imgs.get(i).attributes().get("src");
                        	if (!src.startsWith("http")) {
                        	downloadImage(baseUrl, src);
                        	} else {
                        		if (src.contains(baseUrl)) {
                        			downloadImage(baseUrl, src);
                        		}
                        	}
                        	
                        }
                    }

                
                }
            } catch (Exception e) {
                System.out.println("\n following url page: " + url + " was unable to be read...\n");
                
                e.printStackTrace();
            }

            //This page has fully been visited.
            this.visitedAlready.add(url);

            //Goes to the next linked websites. The higher the number, the deeper the height.
            url = this.goingToVisit.remove(0);
            levelSize = levelSize - 1;

            //Checks to see if the next tier is coming based on the current tier's total queue list size.
            if (levelSize == 0) {
                level = level + 1;
                levelSize = this.goingToVisit.size();
            }

            //If the following url has already been visited, then skip it.
            while (this.visitedAlready.contains(url)) {
                url = this.goingToVisit.remove(0);
                levelSize = levelSize - 1;
                if (levelSize <= 0) {
                    levelSize = this.goingToVisit.size();
                    level = level + 1;
                }
            }
        }
    }
    
    
    // Credits to http://www.compiletimeerror.com/2013/08/java-downloadextract-all-images-from.html#.Vr6KyObpxfY 
    // for the downloadImage method
    private static void downloadImage(String url, String imgSrc) throws IOException {
        BufferedImage image = null;
        try {
            if (!(imgSrc.startsWith("http"))) {
            	if(imgSrc.startsWith("/")) {
                    url = url + imgSrc;            		
            	} else {
            		url = url + "/" + imgSrc;
            	}

            } else {
                url = imgSrc;
            }
            imgSrc = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
            String imageFormat = null;
            imageFormat = imgSrc.substring(imgSrc.lastIndexOf(".") + 1);
            String imagename = imgSrc.substring(0, imgSrc.lastIndexOf("."));
            String imgPath = null;
            imgPath = "C:/data/images/" + imagename.hashCode() + "." + imageFormat + "";
            URL imageUrl = new URL(url);
            image = ImageIO.read(imageUrl);
            if (image != null) {
                File file = new File(imgPath);
                ImageIO.write(image, imageFormat, file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
