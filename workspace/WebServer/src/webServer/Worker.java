package webServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Worker accept socket use processor analyze request and response
 * 
 * @author huhao
 * 
 */
public class Worker extends Thread {
	private BlockingQueue<Socket> tasks;

	public Worker(BlockingQueue<Socket> tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		boolean flag = true;
		Socket clientSocket = null;
		while (flag) {
			clientSocket = tasks.dequeue();
			try {
				// clientSocket.setSoTimeout(10000);
				InputStreamReader input = new InputStreamReader(
						clientSocket.getInputStream());
				BufferedReader reader = new BufferedReader(input);
				PrintStream writer = new PrintStream(
						clientSocket.getOutputStream(), true);
				requestReponseProcessor processer = new requestReponseProcessor(
						reader, writer);
				processer.readRequest();
				processer.writeResponse();

				writer.close();
				input.close();
				reader.close();
				clientSocket.close();

			} catch (SocketException e) {
				e.printStackTrace();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
