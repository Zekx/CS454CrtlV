package Homework;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Image {

	int height, width;         //Size of the image
	HashMap<String, RGB> colors;
	String filePath;
	String fileName;

	public class RGB
	{
		
		int red, blue, green;
			
		public RGB(int red, int green, int blue)
		{
			this.red = red;
			this.blue = blue;
			this.green = green;
		
		}
		
		public int getRed()
		{
			return red;
		}
		
		public int getGreen()
		{
			return green;
		}
		
		public int getBlue()
		{
			return blue;
		}
		public String toString()
		{
			return getRed() + " - " + getGreen() + " - " + getBlue();
		}
	}
	
	public Image()
	{
		this.width = 0;
		this.height = 0;
		this.fileName = "";
		this.filePath = "";
		this.colors = null;
	};
	
	
	//Regular image loading
	public Image(File file)
	{
		BufferedImage img = null;
		try
		{
			String path = file.getAbsolutePath().replace("\\", "/");
			img = ImageIO.read(new FileInputStream(path));
			this.width = img.getWidth();
			this.height = img.getHeight();
			this.filePath = file.getPath();
			this.fileName = file.getName();
			this.colors = new HashMap<String, RGB>();
			for(int w = 0; w < this.width; w ++)
			{
				for(int h = 0; h < this.height; h ++)
				{
					int rgb = img.getRGB(w, h);
					this.colors.put(w + ":" + h, 
							new RGB((rgb >> 16) & 0x000000FF,
									(rgb >> 8) & 0x000000FF,
									(rgb) & 0x000000FF));
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	//This also uploads to database
	public void uploadImage(File file, String url, DBCollection table)
	{
		BufferedImage img = null;
		JsonObjectBuilder jsonLocation = Json.createObjectBuilder();
		int width, height;
		String filePath, fileName;
		try
		{
			String path = file.getAbsolutePath().replace("\\", "/");
			img = ImageIO.read(new FileInputStream(path));
			width = img.getWidth();
			height = img.getHeight();
			filePath = file.getPath();
			fileName = file.getName();
			colors = new HashMap<String, RGB>();
			for(int w = 0; w < width; w ++)
			{
				for(int h = 0; h < height; h ++)
				{
					JsonObjectBuilder jsonColor = Json.createObjectBuilder();

					int rgb = img.getRGB(w, h);
					colors.put(w + ":" + h, 
							new RGB((rgb >> 16) & 0x000000FF,
									(rgb >> 8) & 0x000000FF,
									(rgb) & 0x000000FF));
					jsonColor.add("red", (rgb >> 16) & 0x000000FF)
					.add("green",(rgb >> 8) & 0x000000FF)
					.add("blue", (rgb) & 0x000000FF).build();
					
					jsonLocation.add(w+":"+h, jsonColor);
				}
			}
			
			DBObject image = new BasicDBObject()
					.append("name", fileName)
					.append("url", url)
					.append("path", filePath)
					.append("width", width)
					.append("height", height)
					.append("color", jsonLocation);
			table.insert(image);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getHeight()
	{
		return height;
	}
	public int getWidth()
	{
		return width;
	}
	public HashMap<String, RGB> getColors()
	{
		return colors;
	}
	
	public String getPath()
	{
		return filePath;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public String toString()
	{
		return "File name: " + getFileName() +
				"\nPath: " + getPath() + 
				"\nResolution: " + getHeight() + " x " + getWidth();
	}
	
}
