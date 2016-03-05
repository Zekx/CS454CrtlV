package Homework;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeskThreads implements Runnable{
	public final Object lock = new Object();
	private List<Thread> threads;
	
	public File[] file;
	public Extractor ext;
	public String url;
	
	public Set<String> visitedAlready;
	
		public DeskThreads(int threads, File[] file, Extractor ext, String url){
			this.file = file;
			this.ext = ext;
			this.url = url;
			
			visitedAlready = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
			
			this.threads = new ArrayList<Thread>();
	    	for(int i = 0; i < threads; i++){
	    		this.threads.add(new Thread(new DesktopCrawler(this)));
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
