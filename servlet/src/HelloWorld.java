package com.britesnow.javabench;

import com.britesnow.javabench.Item;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.nio.file.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class HelloWorld extends AbstractHandler
{
	private static int LOOP_SIZE = 100;

	private static int DATA_LINES = 10000;
	private static String DATA_FILE = "./~data/data.txt";
	
	public HelloWorld(){
		StringBuilder sb = new StringBuilder();
		
		String desc = "some very very long very very long very very long very very long very very long very very long very very long very very " + 
		"long very very long very very long very very long very very long very very long very very long description";
		for (int i = 0; i < DATA_LINES; i++ ){
			sb.append(i).append(',');
			sb.append(" item-").append(i).append(',');
			sb.append(desc).append(desc).append(desc).append(" - ").append(i);
			sb.append('\n');
		} 

		Path path = Paths.get(DATA_FILE);
		try{
			Files.createDirectories(path.getParent());
			Files.write(path, sb.toString().getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void handle( String target,
											Request baseRequest,
											HttpServletRequest request,
											HttpServletResponse response ) throws IOException,
																										ServletException
	{
		// System.out.println("-" + target + "-");
		if (target.equals("/")){
			handleHello(target, baseRequest, request, response);
		} else if (target.equals("/loop")){
			handleLoop(target, baseRequest, request, response);
		} else if (target.equals("/list")){
			handleList(target, baseRequest, request, response);
		} else if (target.equals("/data")){
			handleData(target, baseRequest, request, response);
		} 
	}

	void handleData(String target,
											Request baseRequest,
											HttpServletRequest request,
											HttpServletResponse response ) throws IOException,
																										ServletException
	{
		String fileName = DATA_FILE;

		Path path = Paths.get(fileName);
		try{
			byte[] bytes = Files.readAllBytes(path);
			String content = new String(bytes, UTF_8);
			String[] lines = content.split("\n");
			for (String line : lines){
				String[] cells = line.split(",");
				int id = Integer.parseInt(cells[0]);
			}
			response.getWriter().println("<h1>File size: " + content.length() + " </h1>");

	
		}catch(Exception e){
			e.printStackTrace();
		}

		// Inform jetty that this request has now been handled
		baseRequest.setHandled(true);		
	}

	void handleList(String target,
											Request baseRequest,
											HttpServletRequest request,
											HttpServletResponse response ) throws IOException,
																										ServletException
	{
		// Declare response encoding and types
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		final String paramIt = request.getParameter("it");
		final int loop_size = (paramIt != null) ? Integer.parseInt(paramIt) : LOOP_SIZE;

		// create raw datat
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < loop_size; i++ ){
			sb.append(i);
			sb.append(" item-").append(i);
			sb.append("\n");
		}
		final String buf = sb.toString();

		// split
		final String[] lines = buf.split("\\r?\\n");

		// create list
		final List<Item> items = new ArrayList<Item>();
		for (String line : lines){
			final String[] vals = line.split(" ");
			final Item item = new Item(Integer.parseInt(vals[0]), vals[1]);
			items.add(item);
		}

		response.getWriter().println("<h1>Loop Size: " + loop_size + " items.size: " + items.size() + " </h1>");

		// Inform jetty that this request has now been handled
		baseRequest.setHandled(true);

	}


	void handleLoop(String target,
											Request baseRequest,
											HttpServletRequest request,
											HttpServletResponse response ) throws IOException,
																										ServletException{
		// Declare response encoding and types
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		final String paramIt = request.getParameter("it");
		final int loop_size = (paramIt != null) ? Integer.parseInt(paramIt) : LOOP_SIZE;
		
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < loop_size; i++ ){
			sb.append("item ").append(i).append("\n");
		}
		final String buf = sb.toString();

		// Write back response
		response.getWriter().println("<h1>Loop Size: " + loop_size + " Buf Size: " + buf.length() + " </h1>");

		// Inform jetty that this request has now been handled
		baseRequest.setHandled(true);
	}


	void handleHello(String target,
							Request baseRequest,
							HttpServletRequest request,
							HttpServletResponse response ) throws IOException,
																						ServletException{
		// Declare response encoding and types
		response.setContentType("text/html; charset=utf-8");

		// Declare response status code
		response.setStatus(HttpServletResponse.SC_OK);

		// Write back response
		response.getWriter().println("<h1>Hello World</h1> " + target);

		// Inform jetty that this request has now been handled
		baseRequest.setHandled(true);
	}

	public static void main( String[] args ) throws Exception
	{
		Server server = new Server(8080);
		// ((ThreadPool.SizedThreadPool)server.getThreadPool()).setMaxThreads(12);
		System.out.println("Threadpool max: " +  ((ThreadPool.SizedThreadPool)server.getThreadPool()).getMaxThreads​());
		server.setHandler(new HelloWorld());

		server.start();
		server.join();
	}
}
