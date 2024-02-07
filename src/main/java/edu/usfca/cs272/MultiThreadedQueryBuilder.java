package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * Multi-threaded implementation of QueryBuilder that uses inner static class to
 * build the Query line by line
 * 
 * @author troy
 *
 */
public class MultiThreadedQueryBuilder implements Query {

	/**
	 * TreeMap data structure that maps each query word to its word count, score,
	 * and file
	 */
	private final TreeMap<String, ArrayList<SearchResult>> query;

	/**
	 * InvertedIndex object that is initialized to obtain data from invertedIndex
	 * data structure
	 */
	private final ThreadSafeInvertedIndex data;

	/**
	 * WorkQueue object to use to add Task
	 */
	private final WorkQueue queue;

	/**
	 * Constructor that initializes a new TreeSet and TreeMap for queryWords and
	 * query
	 * 
	 * @param data  InvertedIndex object to set
	 * @param queue WorkQueue object to use
	 */
	public MultiThreadedQueryBuilder(ThreadSafeInvertedIndex data, WorkQueue queue) {
		query = new TreeMap<>();
		this.data = data;
		this.queue = queue;
	}

	/**
	 * Multi-threaded implementation that searches the ThreadSafeInvertedIndex and
	 * builds the Query data structure
	 * 
	 * @param path    Path to use
	 * @param partial boolean flag that determines either partial or exact search
	 * @throws IOException If an IO Exception occurs
	 */
	@Override
	public void buildQueries(Path path, boolean partial) throws IOException {
		Query.super.buildQueries(path, partial);
		queue.finish();
	}

	/**
	 * Builds query data structure for words in line that is populated with the
	 * necessary information
	 * 
	 * @param line    Line to stem
	 * @param partial Checks if partial search is conducted
	 */
	@Override
	public void buildQueries(String line, boolean partial) {
		queue.execute(new Task(line, partial));
	}

	/**
	 * Adds the data from another QueryBuilder to this QueryBuilder
	 * 
	 * @param other QueryBuilder object to copy data
	 */
	@Override
	public void addAll(QueryBuilder other) {
		synchronized (query) {
			for (String currentQuery : other.viewQueries()) {
				query.putIfAbsent(currentQuery, new ArrayList<>());
				for (SearchResult result : other.viewSearchResults(currentQuery)) {
					if (!hasSearchResult(currentQuery, result)) {
						query.get(currentQuery).add(result);
					}
				}
			}
		}
	}

	/**
	 * @return an unmodifiable view of the query words in query
	 */
	@Override
	public Set<String> viewQueries() {
		synchronized (query) {
			return Collections.unmodifiableSet(query.keySet());
		}
	}

	/**
	 * Returns an unmodifiable view of the Collection of SearchResults associated
	 * with a word
	 * 
	 * @param line line to stem and search
	 * @return an unmodifiable view of the Collection of SearchResults associated
	 *         with a word
	 */
	@Override
	public Collection<SearchResult> viewSearchResults(String line) {
		synchronized (query) {
			TreeSet<String> words = FileStemmer.uniqueStems(line); // Stems line to get unique words
			String queryLine = ""; // Initialize String value that is used as the key in query
			if (!words.isEmpty()) {
				queryLine = String.join(" ", words); // Joins words together with " "
			}
			ArrayList<SearchResult> results = query.get(queryLine);
			if (results != null) {
				return Collections.unmodifiableCollection(results);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Writes query to Json
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO error occurs
	 */
	@Override
	public void writeQuery(Path path) throws IOException {
		synchronized (query) {
			try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
				Query.writeQuery(query, writer, 0);
			}
		}
	}

	/**
	 * Logger object for debugging purposes
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * @author troy
	 *
	 */
	private class Task implements Runnable {

		/**
		 * Query line to use
		 */
		private final String line;

		/**
		 * boolean that determines partial or exact search
		 */
		private final boolean partial;

		/**
		 * Task constructor that sets the necessary parameters for multi-threading query
		 * and search
		 * 
		 * @param line    Query line to use
		 * @param partial boolean that determines partial or exact search
		 */
		private Task(String line, boolean partial) {
			this.line = line;
			this.partial = partial;
		}

		@Override
		public void run() {
			TreeSet<String> words = FileStemmer.uniqueStems(line);
			if (words.isEmpty()) {
				return;
			}
			String key = String.join(" ", words);

			synchronized (query) {
				if (query.containsKey(key)) {
					return;
				}
				query.put(key, null);
			}
			var local = data.search(words, partial);

			synchronized (query) {
				query.put(key, local);
			}

			log.debug("Completed run() method on: " + line);
		}
	}
}