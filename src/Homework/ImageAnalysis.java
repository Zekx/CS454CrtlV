package Homework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import Homework.Image;
import Homework.Image.RGB;

public class ImageAnalysis {
	
	
	//This method is only for when the two image size are the same
	public static double compare(Image image1, Image image2)
	{
		long diff = 0;
		HashMap<String, RGB> image1Color = image1.getColors();
		HashMap<String, RGB> image2Color = image2.getColors();

			// This section is for when the two image has the same resolution
			 
			for(int w = 0; w < image1.getWidth(); w ++)
			{
				for(int h = 0; h < image1.getHeight(); h ++)
				{
					RGB image1RGB = image1Color.get(w+":"+h);
					RGB image2RGB = image2Color.get(w+":"+h);
					diff += Math.abs(image1RGB.getRed() - image2RGB.getRed());
					diff += Math.abs(image1RGB.getGreen() - image2RGB.getGreen());
					diff += Math.abs(image1RGB.getBlue() - image2RGB.getBlue());
				}
			}
			double n = (image1.getWidth() * image1.getHeight()) ;
			double p = diff / n / 255.0;
			return p;

	}
	
	public static List<Document> searchDatabase(Image image)
	{
		//This methods searches the database using image dimensions
		MongoClient mongo = new MongoClient("127.0.0.1", 27017);
		MongoDatabase db = mongo.getDatabase("crawler");
		MongoCollection<Document> inventory = db.getCollection("images");

		List<Document> list = new ArrayList<Document>();
		
		inventory.find(
				Filters.and(
						Filters.eq("width", image.getWidth()), 
						Filters.eq("height", image.getHeight()))).into(list);
		System.out.println(list.size());
		for(int i = 0; i < list.size(); i ++)
		{
			System.out.println(list.get(i));
		}
		return list;
	}
	
	/* This method is for local searching, too inefficient
	 * Only meant for testing
	 */
	public static List<Image> searchImage(Image testImage)
	{
		List<Image> result = new ArrayList<Image>();
		
		File[] folder = (new File("C:/Users/Rose/Desktop/ImageTest")).listFiles();
		for(File file: folder)
		{
			//System.out.println(file.getAbsolutePath());
			double difference = 0;
			Image imageFile;
			if(!file.isDirectory())
			{
				imageFile = new Image(file);
				//System.out.println("\n" + imageFile.toString());
				//System.out.println("==========================");
				if(testImage.getHeight() == imageFile.getHeight() && testImage.getWidth() == imageFile.getWidth())
				{
					difference = compare(testImage, imageFile);
					System.out.println("Diff: " + difference);
					if(difference <= 0.6)
					{
						result.add(imageFile);
					}
			
				}
			}
			
		}
		
		
		return result;
	}

	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		/*
		 * File[] folder = (new File("C:/Users/Rose/Desktop/ImageTest/").listFiles());
		Image image = new Image();
		for(File file: folder)
		{
			image.uploadImage(file, file.getAbsolutePath(), table);
		}
		*/
   
		/*Image testImage = new Image(new File("C:/Users/Rose/Desktop/ImageTest/tumblr_ms101qZCsC1sajfz4o1_500.jpg"));
		System.out.println(compare(testImage, testImage));
		List<Document> list = searchDatabase(testImage);
		for(int i = 0; i < list.size(); i ++)
		{
			Document doc = list.get(i);
			
			System.out.println(compare(testImage, new Image(new File( doc.get("path").toString()))));
		}*/
	}
	
}
