package webServer;

import java.net.Socket;

public class ThreadPool{
	private BlockingQueue<Socket> tasks;
	private Worker[] workers;

	public ThreadPool(int taskLimit, int workerNumber) {
		workers = new Worker[workerNumber];
		tasks = new BlockingQueue<>(taskLimit);
		for (int i = 0 ; i < workers.length; i ++){
			workers[i] = new Worker(tasks); 
		}
		for (int i = 0; i < workers.length; i ++){
			workers[i].start();
		}
		
	}
	
	public void acceptTask(Socket t){
		tasks.enqueue(t);
	}
	


}
