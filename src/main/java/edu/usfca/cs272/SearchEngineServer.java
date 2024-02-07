package edu.usfca.cs272;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Creates a Search Engine page using Jetty and Servlets.
 * 
 * @author troy
 *
 */
public class SearchEngineServer {
	/**
	 * Creates a server on the provided port and the InvertedIndex object to use
	 * 
	 * @param port  Port to bind to
	 * @param data  InvertedIndex object for Servlet to use
	 * @param queue WorkQueue object for Servlet to use
	 * @throws Exception   If an Exception occurs
	 * @throws IOException If an IO error occurs
	 */
	public void startServer(int port, ThreadSafeInvertedIndex data, WorkQueue queue) throws Exception {
		Server server = new Server(port);

		ServletHandler handler = new ServletHandler();

		handler.addServletWithMapping(new ServletHolder(new SearchResultServlet(data, queue)), "/");

		server.setHandler(handler);
		server.start();
		server.join();
	}
}
