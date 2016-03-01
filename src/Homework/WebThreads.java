package Homework;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class WebThreads implements Runnable{
	public final Object lock = new Object();
	private List<Thread> threads;
	
	public BlockingQueue<String> goingToVisit;
	public BlockingQueue<String> visitedAlready;
	
	public String url = "";
    
    public boolean extract = false;
    
    public int height = 0;
    public int level = 0;
    public int levelSize = 0;
    
    public WebThreads(int threads, String u, Integer d, boolean e){
    	this.url = u;
    	this.height = d;
    	this.extract = e;
    	this.levelSize = 1;
    	
    	goingToVisit = new LinkedBlockingDeque<String>();
    	goingToVisit.add(this.url);
    	
    	visitedAlready = new LinkedBlockingDeque<String>();
    	
    	this.threads = new ArrayList<Thread>();
    	for(int i = 0; i < threads; i++){
    		this.threads.add(new Thread(new Crawler(this)));
    	}
    }

	@Override
	public void run() {
		for(Thread t: threads){
			t.start();
		}
		boolean threadsDone = false;
		while(!threadsDone){
			threadsDone = true;
			for(int i = 0; i < threads.size(); i++){
				if(threads.get(i).isAlive()){
					Thread.yield();
					i = 0;
					threadsDone = false;
				}
			}
		}
	}
}
