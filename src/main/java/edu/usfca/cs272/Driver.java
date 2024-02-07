package edu.usfca.cs272;

//import java.nio.file.NoSuchFileException;
import java.io.IOException;
import java.nio.file.Path;
//import java.io.Writer;
import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Troy Sorongon
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class Driver {

	/**
	 * For logging
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		/*
		 * Initializes objects necessary for building and searching inverted index data
		 * structure
		 */
		ArgumentParser parser = new ArgumentParser(args);
		InvertedIndex data;
		Query query;
		ThreadSafeInvertedIndex threadSafe = null;
		WorkQueue queue = null;
		Crawler crawler = null;
		SearchEngineServer searchEngine = null;

		if (parser.hasFlag("-threads") || parser.hasFlag("-html") || parser.hasFlag("-server")) { // Allows for
																									// multi-threading,
																									// changes inverted
			// index and query to be thread safe
			int threads = parser.getInteger("-threads", 5);
			if (threads < 1) {
				threads = 5;
			}

			/* Sets ThreadSafeInvertedIndex */
			threadSafe = new ThreadSafeInvertedIndex();
			data = threadSafe;
			/* Initializes WorkQueue to be used for query */
			queue = new WorkQueue(threads);
			query = new MultiThreadedQueryBuilder(threadSafe, queue);

			/* ----- Crawler ----- */
			crawler = new Crawler(threadSafe, queue);
			String input = parser.getString("-html");

			int maxURLs = 1;
			if (parser.hasFlag("-crawl")) {
				maxURLs = parser.getInteger("-crawl");
			}

			try {
				crawler.multiThreadCrawl(input, maxURLs);
			} catch (Exception e) {
				System.err.println("Error with -html");
				log.catching(Level.ERROR, e);
			}

			/* ----- Server ----- */
			searchEngine = new SearchEngineServer();
			int port = parser.getInteger("-server", 8080);
			System.out.println("Port: " + port);
			System.out.println("starting Server");

			try {
				searchEngine.startServer(port, threadSafe, queue);
				System.out.println("starting server");
			} catch (Exception e) {
				System.err.println("Error with server");
				log.catching(Level.ERROR, e);
			}

		} else { // Uses single threaded implementation
			data = new InvertedIndex();
			query = new QueryBuilder(data);
		}

		/* "-text" */
		if (parser.hasFlag("-text")) {
			try {
				Path input = parser.getPath("-text");
				if (threadSafe != null && queue != null) {
					log.debug("Starting multi-threaded build...");

					MultiThreadedInvertedIndexBuilder.build(input, threadSafe, queue);

					log.debug("Finsihed multi-threaded build...");
				} else {
					InvertedIndexBuilder.build(input, data);
				}
			} catch (NullPointerException | IOException | IllegalArgumentException e) {
				System.err.println("Error with -text path");
				log.catching(Level.ERROR, e);
			}

		}

		/* "-query" */
		if (parser.hasFlag("-query")) {
			Path queryInput = parser.getPath("-query");
			try {
				query.buildQueries(queryInput, parser.hasFlag("-partial"));
			} catch (NullPointerException | IOException e) {
				System.err.println("Error building Query and/or search");
			}
		}

		if (queue != null) {
			queue.shutdown();
		}

		/* "-counts" */
		if (parser.hasFlag("-counts")) {
			Path countsOutput = parser.getPath("-counts", Path.of("counts.json"));
			try {
				data.writeCounts(countsOutput); // Write to JSON file
			} catch (IOException e) {
				System.err.println("Error writing to the counts JSON file at: " + countsOutput);
			}
		}

		/* "-index" */
		if (parser.hasFlag("-index")) {
			Path indexOutput = parser.getPath("-index", Path.of("index.json"));
			try {
				data.writeIndex(indexOutput); // Write to JSON file
			} catch (NullPointerException | IOException e) {
				System.err.println("invalid '-index'");
			}
		}

		/* "-results" */
		if (parser.hasFlag("-results")) {
			Path resultsOutput = parser.getPath("-results", Path.of("results.json"));
			try {
				query.writeQuery(resultsOutput);
			} catch (NullPointerException | IOException e) {
				System.err.println("Error writing Query to JSON");
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}