package webServer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * http://tutorials.jenkov.com/java-concurrency/blocking-queues.html
 * @param <T>
 */
public class BlockingQueue <T>{
	private Queue<T> q;
	private int limit = 10000;

	public BlockingQueue(int limit) {
		if(limit <= 0){
			new IllegalArgumentException();
		}
		this.limit = limit;
		q = new LinkedList<T>();
	}
	
	public synchronized void enqueue(T t){
		while(q.size() == limit){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(q.size()  == 0){
			notifyAll();
		}
		q.add(t);
	}
	
	public synchronized T dequeue() {
		while(q.size() == 0){
			try{
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		if(this.limit == q.size()){
			notifyAll();
		}
		return q.remove();
	}

}
