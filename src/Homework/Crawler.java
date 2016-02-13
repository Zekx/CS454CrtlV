package Homework;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
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
    private Map<String, String> pageNames = new HashMap<String, String>();
    private Set<String> visitedAlready = new HashSet<String>();

    public Map<String, String> getPageNames(){
        return pageNames;
    }

    public void urlCrawler(String url, int height, Boolean extract, int level, int levelSize, DBCollection db) {
        while (level <= height) {
            System.out.println(url + " Tier:" + level + " current Tier Size:" + levelSize + " Current Total Size:" + this.goingToVisit.size());

            try {
                if (!url.replace(" ", "").isEmpty()) {
                    Connection connection = Jsoup.connect(url);
                    Connection.Response resp = Jsoup.connect(url).timeout(100 * 1000).ignoreHttpErrors(true).followRedirects(true).execute();
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

                        this.pageNames.put( connection.maxBodySize(Integer.MAX_VALUE).get().title() , url);

                        if(extract){
                            String htmlTitle = connection.maxBodySize(Integer.MAX_VALUE).get().title();
                            File newHtmlFile = new File("C:/data/htmls/"+htmlTitle+".html");
                            FileUtils.writeStringToFile(newHtmlFile, resp.body());
                        }
                        if(extract){
                            System.out.println("ELEMENTS WITH IMG " + doc.getElementsByAttribute("src"));
                            String baseUrl = url.substring(0, url.indexOf("/", 7));
                            Elements imgs = doc.getElementsByTag("img");
                            for(int i = 0; i < imgs.size(); i++) {
                                String src = imgs.get(i).attributes().get("src");
                                downloadImage(baseUrl, src);
                            }
                        }
                        // TODO: Need to somehow implement img downloading and videos
                        // Maybe check for attribute tag for img and then check src is url is same then download as jpg/mp4?
//                        BasicDBObject mongoDoc = new BasicDBObject()
//                        		.append("name", htmlTitle)
//                        		.append("url", url)
//                        		.append("creationTime", System.currentTimeMillis())
//                        		// Changed this to be the response body because this is the legit html src.
//                        		.append("HTML_Text", resp.body());
//                        db.insert(mongoDoc);
//                        System.out.println(htmlTitle);
                    }
                }
            } catch (Exception e) {
                System.out.println("\n following url page: " + url + " was unable to be read...\n");
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
                url = url + imgSrc;
            } else {
                url = imgSrc;
            }
            imgSrc = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
            String imageFormat = null;
            imageFormat = imgSrc.substring(imgSrc.lastIndexOf(".") + 1);
            String imgPath = null;
            imgPath = "C:/data/images/" + imgSrc + "";
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

