package webServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import static java.lang.System.out;

/**
 * implements a simple multithreaded httpServer
 * 
 * @author huhao
 * 
 */
public class HttpServer {
	public static ServerSocket server;
	public static int port = 8100;
	private static boolean flag = true;
	private static String root = "/mnt/castor/seas_home/h/huhao/workspace/WebServer/WebSite";

	/**
	 * 
	 * @param args
	 *            arg[0] redefine server root default is
	 *            /mnt/castor/seas_home/h/huhao/workspace/WebServer arg[1]
	 *            redefine port number default is 8100
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 1) {
			out.print("Error");
			port = Integer.valueOf(args[0]);
		} else if (args.length == 2) {
			root = args[1];
			port = Integer.valueOf(args[0]);
		}
		out.println("Hao's Server");
		out.println("Port: " + port + "\n Root Directory: " + root);
		ThreadPool pool = new ThreadPool(10000, 10);
		try {
			server = new ServerSocket(port, 300000);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (flag) {
			Socket s = null;
			try {
				s = server.accept();
				pool.acceptTask(s);
			} catch (SocketException e) {
				System.err.println("Server already closed");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (!flag)
					break;
			}
		}
		out.println("Server Closed");
	}

	public static void closeServer() {
		flag = false;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getRoot() {
		return root;
	}

	public static void setRoot(String root) {
		HttpServer.root = root;
	}

}
