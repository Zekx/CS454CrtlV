package Homework;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by NIck on 2/6/2016.
 */
public class Extractor {

    //This method extracts the specific document and outputs a set of string
    //Not sure if we should keep track of the count per word
    public static Set<String> extract(File file)  throws IOException, SAXException, TikaException
    {
        Set<String> set = new HashSet<String>();

        //I heard wrapping the FileInputStream in BufferedInputStream is faster, idk if it actually is
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file.toString()))) {

            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();

            parser.parse(stream, handler, metadata);

            set.add(handler.toString().replaceAll("\\s+"," ")); //This should remove most of the white spaces

        } catch (TikaException e) {
            e.printStackTrace();
        }

        return set;
    }

    //This extracts the information to a JSON file
    public void exportJson()
    {
        //
    }

    public static void main(String[] args) throws IOException, SAXException, TikaException
    {

        File file = new File ("Insert file path here");
        Set<String> set = extract(file);
        /*File[] fileList = file.listFiles();*/
        for(String s: set)
        {
            System.out.println(s);
        }



    }

}
