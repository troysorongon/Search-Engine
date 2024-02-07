package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Crawls through a given URL
 * 
 * @author troy
 *
 */
public class Crawler {

	/**
	 * WorkQueue object to use
	 */
	private final WorkQueue queue;

	/**
	 * ThreadSafeInvertedIndex object to build
	 */
	private final ThreadSafeInvertedIndex data;

	/**
	 * Contructor for Crawler object that sets the ThreadSafeInvertedIndex and
	 * WorkQueue
	 * 
	 * @param data  ThreadSafeInvertedIndex object to set
	 * @param queue WorkQueue to set
	 */
	public Crawler(ThreadSafeInvertedIndex data, WorkQueue queue) {
		this.data = data;
		this.queue = queue;
	}

	/**
	 * Returns an ArrayList of stems of the HTML after being stripped
	 * 
	 * @param url URL to fetch
	 * @return Returns the stems of the HTML after being stripped
	 */
	public static ArrayList<String> parseHtml(URL url) {
		String html = HtmlFetcher.fetch(url, 3);
		html = HtmlCleaner.stripHtml(html);
		return FileStemmer.listStems(html);
	}

	/**
	 * Converts a seed URL to URL type then strips HTML string found from fetching
	 * the URL. The String is then stemmed to get each individual stem in the string
	 * then adds those to the InvertedIndex data structure
	 * 
	 * @param seedUrl Starting Url
	 * @throws MalformedURLException If this Exception occurs
	 * @throws URISyntaxException    If this Exception occurs
	 */
	public void crawl(String seedUrl) throws MalformedURLException, URISyntaxException {
		URL url = new URL(seedUrl);
		url = LinkFinder.normalize(url);
		ArrayList<String> parsed = parseHtml(url);
		int counter = 1;
		for (String word : parsed) {
			data.add(word, url.toString(), counter++);
		}
	}

	/**
	 * Multithreaded implementation of building the ThreadSafeInvertedIndex object
	 * starting with a seedUrl
	 * 
	 * @param seedUrl URL to crawl
	 * @param max     Max number of URLs to crawl
	 * @throws MalformedURLException If this error occurs
	 * @throws URISyntaxException    If this error occurs
	 */
	public void multiThreadCrawl(String seedUrl, int max) throws MalformedURLException, URISyntaxException {
		// int maxCounter = 1;
		HashSet<URL> crawled = new HashSet<>(); // O(1) lookup time
		URL url = new URL(seedUrl);
		crawled.add(url);
		queue.execute(new Task(url, max, crawled, data, queue));
		queue.finish();
	}

	/**
	 * Tasks for the WorkQueue object that recursively crawls links to build the
	 * ThreadSafeInvertedIndex object
	 * 
	 * @author troy
	 *
	 */
	private class Task implements Runnable {

		/**
		 * Query line to use
		 */
		private URL url;

		/**
		 * Max number of URLs to crawl
		 */
		private final int max;

		/**
		 * HashSet that keeps track of all the visited URLs
		 */
		private final HashSet<URL> crawled;

		/**
		 * ThreadSafeInvertedIndex object to build
		 */
		private final ThreadSafeInvertedIndex data;

		/**
		 * WorkQueue object to use
		 */
		private final WorkQueue queue;

		/**
		 * Task constructor that sets the necessary parameters for multi-threading query
		 * and search
		 * 
		 * @param url     URL to crawl
		 * @param max     Max number of URLs to crawl
		 * @param data    ThreadSafeInvertedIndex to build
		 * @param queue   WorkQueue to use
		 * @param crawled HashSet that keeps track of all the visited URLs
		 * 
		 */
		private Task(URL url, int max, HashSet<URL> crawled, ThreadSafeInvertedIndex data, WorkQueue queue) {
			this.url = url;
			this.max = max;
			this.crawled = crawled;
			this.data = data;
			this.queue = queue;
		}

		@Override
		public void run() {
			// Get the length of visited to know counter
			try {
				url = LinkFinder.normalize(url);
				String html = HtmlFetcher.fetch(url, 3);
				if (html != null) {
					html = HtmlCleaner.stripBlockElements(html);

					ArrayList<URL> list = LinkFinder.listUrls(url, html); // normalized URLs
					for (URL curr : list) {
						synchronized (crawled) {
							if (crawled.size() < max && !crawled.contains(curr)) {
								queue.execute(new Task(curr, max, crawled, data, queue));
								crawled.add(curr);
							}
						}
					}

					ArrayList<String> parsed = parseHtml(url);
					int counter = 1;
					for (String word : parsed) {
						data.add(word, url.toString(), counter++);
					}
				}

			} catch (Exception e) {
				System.err.println("Error while running Task");
			}
		}
	}

}
