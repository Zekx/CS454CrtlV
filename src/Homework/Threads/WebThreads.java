package Homework.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.mongodb.DB;
import com.mongodb.DBCollection;

import Homework.Crawler;

public class WebThreads implements Runnable{
	public final Object lock = new Object();
	private List<Thread> threads;
	
	public BlockingQueue<String> goingToVisit;
	public Set<String> visitedAlready;
	
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
    	
    	visitedAlready = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    	
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
		for(Thread t: threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
