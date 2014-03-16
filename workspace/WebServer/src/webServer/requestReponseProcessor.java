package webServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TimeZone;

/**
 * Analyze request and do response
 * 
 * @author huhao
 * 
 */
public class requestReponseProcessor {
	private enum methods {
		GET, POST, HEAD, BAD
	}

	private BufferedReader reader;
	private PrintStream writer;
	private ArrayList<String> request;
	private methods method;
	private String path;
	private boolean isSecurity;
	private Hashtable<String, String> responseInformation;
	private String http;
	private Hashtable<String, ArrayList<String>> headDic;

	public requestReponseProcessor(BufferedReader reader, PrintStream writer) {
		this.reader = reader;
		this.writer = writer;
		request = new ArrayList<String>();
		http = null;
		headDic = new Hashtable<>();
		responseInformation = new Hashtable<>();
	}

	public void readRequest() {
		String str = "";
		do {
			try {
				str = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			request.add(str.trim());
			System.out.println(str);
		} while (str != null && !str.equals(""));
		if (request.isEmpty())
			return; // err
		parseInitialLine(request.get(0));
		parseHeadLine();
		checkRequest();
	}

	/**
	 * Check request and prepare response information if err encountered
	 */
	public void checkRequest() {
		if (http.equals("notSupporsted") || method == methods.BAD) {
			responseInformation.put("code", "400");
			responseInformation.put("message", http + " 400 Bad request");
			return;
		}
		isSecurity = isPathSecurity(path);
		if (!isSecurity) {
			responseInformation.put("code", "403");
			responseInformation.put("message", http + " 403 Forbidden");
			return;
		}
		if (http.equals("HTTP/1.0")) {
			if (request.get(1).contains("http://")) {
				responseInformation.put("code", "404");
				responseInformation.put("message",
						"HTTP/1.0 Not Support Absolute Path: 404 Not Found");
				return;
			}
		} else if (http.equals("HTTP/1.1")) {
			if (headDic.get("Host") == null) {
				responseInformation.put("code", "404");
				responseInformation.put("message", "No Host!: 404 Not Found");
				return;
			} else if (!headDic.get("Host").get(0)
					.equals(("localhost:" + Integer.toString(HttpServer.port)))) {
				System.out.println("");
				responseInformation.put("code", "400");
				responseInformation.put("message",
						"Host incorrect: header received: 400 Not Found");
				return;
			}
		} else {
			System.err.println("Not supported");
		}
		responseInformation.put("code", "200");
		responseInformation.put("message", "OK");
		return;
	}

	/**
	 * Writes response
	 */
	public void writeResponse() {
		// write initial line, if bad request , response immediately
		// here bad request means protocol undefined method unsupported or
		// path violates security
		if (!responseInformation.get("code").equals("200")) {
			// initial line
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message") + "\r\n\r\n");
			StringBuffer content = new StringBuffer();
			content.append("<html><body>\r\n" + "<h2>"
					+ responseInformation.get("message") + "</h2>\r\n"
					+ "</body></html>\r\n");
			// write header
			writer.append("Content-Type: text/html\r\n" + "Content-Length: "
					+ content.length() + "\r\n\r\n");
			// write content
			writer.append(content);
			return;
		}
		// come here we need further check
		switch (method) {
		case GET:
			doGetResponse();
			break;
		case POST:
			doPostResponse();
			break;
		case HEAD:
			doHeadResponse();
			break;
		case BAD:
			break;
		default:
			return;
		}

	}

	/**
	 * Prepares response when operation is GET
	 */
	private void doGetResponse() {
		File file = new File(HttpServer.getRoot() + path);
		if (file.isDirectory()) {
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message") + "\r\n");
			writeHeader();
			generateDirectoryHtml();
		} else if (file.exists()) {
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message") + "\r\n");
			writeHeader();
			FileInputStream fileStream = null;
			byte[] fileBytes = new byte[(int) file.length()];
			try {
				fileStream = new FileInputStream(file);
				fileStream.read(fileBytes);
				fileStream.close();
				writer.write(fileBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// if file no exist

		} else {
			writer.append(http.toUpperCase() + " " + "404 " + "Not found \r\n");
			responseInformation.put("code", "404");
			responseInformation.put("message", "404 Not Found");
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message") + "\r\n");
			StringBuffer content = new StringBuffer();
			content.append("<html><body>\r\n" + "<h2>"
					+ responseInformation.get("message") + "</h2>\r\n"
					+ "</body></html>\r\n");
			// write header
			writer.append("Content-Type: text/html\r\n" + "Content-Length: "
					+ content.length() + "\r\n\r\n");
			// write content
			writer.append(content);
			return;
		}
	}

	private void doPostResponse() {

	}

	private void doHeadResponse() {
		File file = new File(HttpServer.getRoot() + path);
		if (file.isDirectory()) {
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message"));
			writeHeader();
		} else if (file.exists()) {
			writer.append(http.toUpperCase() + " "
					+ responseInformation.get("code") + " "
					+ responseInformation.get("message") + "\r\n");
			writeHeader();
		} else {
			writer.append(http.toUpperCase() + " " + "404 " + "Not found");
			responseInformation.put("code", "404");
			responseInformation.put("message", "404 Not Found");
			writer.append("Content-Type: text/html\r\n"
					+ "Content-Length: 111\r\n" + "\n" + "<html><body>\r\n"
					+ "<h2>" + responseInformation.get("message") + "</h2>\r\n"
					+ "</body></html>\r\n");

		}
	}

	private void writeHeader() {
		writeTimeHeader();
		writeTypeHeader();
		writer.append("\r\n\r\n");
	}

	private void writeTimeHeader() {
		SimpleDateFormat df1 = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z");
		df1.setTimeZone(TimeZone.getTimeZone("GMT"));
		responseInformation.put("Date", df1.format(new Date()));
		writer.append("Date: " + responseInformation.get("Date"));
	}

	private void writeTypeHeader() {

		if (path.contains(".")) {
			if (path.contains(".gif")) {
				responseInformation.put("Content-Type", "image/gif");
			}
			if (path.contains(".png")) {
				responseInformation.put("Content-Type", "image/png");
			}
			if (path.contains(".jpg")) {
				responseInformation.put("Content-Type", "image/jpeg");
			}
			if (path.contains(".txt")) {
				responseInformation.put("Content-Type", "text/plain");
			}
			if (path.contains(".html")) {
				responseInformation.put("Content-Type", "text/html");
			}
		} else {
			responseInformation.put("Content-Type", "unknown");
		}

		writer.append("Content-Type: "
				+ responseInformation.get("Content-Type"));
	}

	private void generateDirectoryHtml() {
		String currentDirectory = HttpServer.getRoot() + path;
		File fileFolder = new File(currentDirectory);
		File[] fileList = fileFolder.listFiles();
		StringBuffer htmlPageString = new StringBuffer();
		htmlPageString.append("<html>");
		htmlPageString.append("  <body>");
		htmlPageString.append("    <h1>My server</h1>");
		htmlPageString.append("    <h2>Directory</h2>");
		htmlPageString.append("    <hr>");

		for (int i = 0; i < fileList.length; ++i) {
			String filePath = path + "/" + fileList[i].getName();
			String trimmedFilePath = filePath.trim().replaceAll("/+", "/");
			htmlPageString.append("<p>");
			htmlPageString.append("<a href=\"" + trimmedFilePath + "\"" + ">"
					+ fileList[i].getName() + "</a>");
			htmlPageString.append("</p>");
		}
		htmlPageString.append("    </hr>");
		htmlPageString.append(htmlPageString + "  </body>");
		htmlPageString.append(htmlPageString + "</html>");
		writer.append(htmlPageString);
		writer.flush();
	}

	/**
	 * Parses the initial line updates method path and http
	 * 
	 * @param initLine
	 */
	private void parseInitialLine(String line) {
		String[] splitLine = line.trim().split(" ");
		ArrayList<String> words = new ArrayList<>();
		for (int i = 0; i < splitLine.length; ++i) {
			if (!splitLine[i].isEmpty()) {
				words.add(splitLine[i]);
			}
		}
		if (words.size() < 4) {
			// TO DO err
		}

		// update method
		switch (words.get(0)) {
		case "GET":
			method = methods.GET;
			break;
		case "POST":
			method = methods.POST;
			break;
		case "HEAD":
			method = methods.HEAD;
			break;
		default:
			method = methods.BAD;
		}

		if (words.get(2).equalsIgnoreCase("HTTP/1.0")) {
			http = "HTTP/1.0";
		} else if (words.get(2).equalsIgnoreCase("HTTP/1.1")) {
			http = "HTTP/1.1";
		} else if (words.get(2).equalsIgnoreCase("HTTP/1.2")) {
			http = "HTTP/1.2";
		} else {
			http = "notSupporsted";
		}

		try {
			findPath(words.get(1));
		} catch (MalformedURLException e) {
			System.err.println("url err");
		}
	}

	/**
	 * Deal with path under different protocol
	 * 
	 * @param filePath
	 * @throws MalformedURLException
	 */
	private void findPath(String filePath) throws MalformedURLException {
		// http 1.0 do not support absolute path
		if (http.equals("HTTP/1.0") && filePath.contains("http://")) {
			throw new UnsupportedOperationException();
		} else if (http.equals("HTTP/1.1") || http.equals("HTTP/1.2")) {
			if (filePath.contains("http://")) {
				path = new URL(filePath).getPath();
			}
		}
		// come here means not absolute path
		path = filePath;
	}

	/**
	 * Checks security
	 * 
	 * @param fileName
	 * @return
	 */
	private boolean isPathSecurity(String filePath) {
		// "/directory" would split into "" and "directory"
		ArrayList<String> fileList = new ArrayList<String>(
				Arrays.asList(filePath.split("/")));
		Stack<String> fileStack = new Stack<String>();
		for (String file : fileList) {
			if (file.equals("..")) {
				fileStack.pop();
				if (fileStack.isEmpty()) {
					return false;
				}
			} else {
				fileStack.push(file);
			}
		}
		return true;
	}

	private void parseHeadLine() {
		for (int i = 1; i < request.size(); ++i) {

			String[] temp = request.get(i).split(":");
			if (temp.length == 1) {
				ArrayList<String> list = headDic.get(temp[0]);
				if (list == null) {
					list = new ArrayList<>();
					list.add("");
					headDic.put(temp[0].trim(), list);
				} else {
					list.add("");
				}

			} else if (temp.length == 2) {
				ArrayList<String> list = headDic.get(temp[0]);
				if (list == null) {
					list = new ArrayList<>();
					list.add(temp[1]);
					headDic.put(temp[0].trim(), list);
				} else {
					list.add(temp[1]);
				}
			} else if (temp.length == 3) {
				ArrayList<String> list = headDic.get(temp[0]);
				if (list == null) {
					list = new ArrayList<>();
					list.add(temp[1].trim() + ":" + temp[2].trim());
					headDic.put(temp[0].trim(), list);
				} else {
					list.add(temp[1].trim() + ":" + temp[2].trim());
				}
			} else {
				// TO DO
				ArrayList<String> list = headDic.get(temp[0]);
				if (list == null) {
					list = new ArrayList<>();
					list.add(temp[1].trim() + ":" + temp[2].trim());
					headDic.put(temp[0].trim(), list);
				} else {
					list.add(temp[1].trim() + ":" + temp[2].trim());
				}
			}
		}

	}

}