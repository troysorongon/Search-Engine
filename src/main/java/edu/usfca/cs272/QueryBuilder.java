package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * This class builds the query data structure.
 * 
 * @author troy
 *
 */
public class QueryBuilder implements Query {

	/**
	 * TreeMap data structure that maps each query word to its word count, score,
	 * and file
	 */
	private final TreeMap<String, ArrayList<SearchResult>> query;

	/**
	 * InvertedIndex object that is initialized to obtain data from invertedIndex
	 * data structure
	 */
	private final InvertedIndex data;

	/**
	 * Stemmer used for stemming line to get unique stems
	 */
	private final Stemmer stemmer;

	/**
	 * Constructor that initializes a new TreeSet and TreeMap for queryWords and
	 * query
	 * 
	 * @param data InvertedIndex object to set
	 */
	public QueryBuilder(InvertedIndex data) {
		query = new TreeMap<>();
		this.data = data;
		this.stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
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
		TreeSet<String> words = FileStemmer.uniqueStems(line, stemmer); // Stems line to get unique words
		String key = ""; // Initialize String value that is used as the key in query
		if (!words.isEmpty()) {
			key = String.join(" ", words); // Joins words together with " "

			// For the case where there are multiple lines that have the same words after
			// stemming
			if (!query.containsKey(key)) { // If query does contain the key, that means it is already populated so no
											// need to create another ArrayList
				query.put(key, data.search(words, partial));
			}
		}
	}

	/**
	 * Adds the data from another QueryBuilder to this QueryBuilder
	 * 
	 * @param other QueryBuilder object to copy data
	 */
	@Override
	public void addAll(QueryBuilder other) {
		for (String currentQuery : other.viewQueries()) {
			query.putIfAbsent(currentQuery, new ArrayList<>());
			for (SearchResult result : other.viewSearchResults(currentQuery)) {
				if (!hasSearchResult(currentQuery, result)) {
					query.get(currentQuery).add(result);
				}
			}
		}
	}

	/**
	 * @return an unmodifiable view of the query words in query
	 */
	@Override
	public Set<String> viewQueries() {
		return Collections.unmodifiableSet(query.keySet());
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
	public List<SearchResult> viewSearchResults(String line) {
		TreeSet<String> words = FileStemmer.uniqueStems(line, stemmer); // Stems line to get unique words
		String queryLine = ""; // Initialize String value that is used as the key in query
		if (!words.isEmpty()) {
			queryLine = String.join(" ", words); // Joins words together with " "
		}
		ArrayList<SearchResult> results = query.get(queryLine);
		if (results != null) {
			return Collections.unmodifiableList(results);
		}
		return Collections.emptyList();
	}

	/**
	 * Writes query to Json
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO error occurs
	 */
	@Override
	public void writeQuery(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			Query.writeQuery(query, writer, 0);
		}
	}
}
