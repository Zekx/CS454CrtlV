package Homework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import Homework.Image;
import Homework.Image.RGB;

public class ImageAnalysis {
	
	public byte[] toBitMap(File file)
	{
		byte[] image = null;
		
		return image;
	}
	
	
	//This method is only for when the two image size are the same
	public static double compare(Image image1, Image image2)
	{
		long diff = 0;
		HashMap<String, RGB> image1Color = image1.getColors();
		HashMap<String, RGB> image2Color = image2.getColors();
		for(int w = 0; w < image1.getWidth(); w ++)
		{
			for(int h = 0; h < image1.getHeight(); h ++)
			{
				RGB image1RGB = image1Color.get(w+":"+h);
				RGB image2RGB = image2Color.get(w+":"+h);
				diff += Math.abs(image1RGB.getRed() - image2RGB.getRed());
				diff += Math.abs(image1RGB.getRed() - image2RGB.getGreen());
				diff += Math.abs(image1RGB.getRed() - image2RGB.getBlue());
			}
		}
		double n = (image1.getWidth() * image1.getHeight()) ;
		double p = diff / n / 255.0;
		return p;
	}
	
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
		Image testImage = new Image(new File("C:/Users/Rose/Desktop/ImageTest/tumblr_ms101qZCsC1sajfz4o1_500.jpg"));
		
		List<Image> result = searchImage(testImage);
		
		
		if(result.isEmpty())
		{
			System.out.println("No image matched");
		}
		else
		{
			System.out.println(result.size());
			for(int i = 0; i < result.size(); i++)
			{
				System.out.println(result.get(i).toString());
			}
		}
	}
	
}
