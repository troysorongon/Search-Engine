package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import edu.usfca.cs272.InvertedIndex.SearchResult;

/**
 * Framework for single or multi-threaded implementation for building Query.
 * Query data structure stores lines of strings as its keys and an ArrayList of
 * SearchResult objects that store word count, score, and path of that
 * associated key
 * 
 * @author troy
 *
 */
public interface Query {

	/**
	 * Builds query data structure for words in line that is populated with the
	 * necessary information
	 * 
	 * @param line    Line to stem
	 * @param partial Checks if partial search is conducted
	 */
	public void buildQueries(String line, boolean partial);

	/**
	 * Builds query data structure for all lines in the query file with the
	 * necessary information
	 * 
	 * @param path    path to use
	 * @param partial checks if partial search is conducted
	 * @throws IOException If an IO error occurs
	 */
	public default void buildQueries(Path path, boolean partial) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			while (reader.ready()) {
				buildQueries(reader.readLine(), partial);
			}
		}
	}

	/**
	 * Adds the data from another QueryBuilder to this QueryBuilder
	 * 
	 * @param other QueryBuilder object to copy data
	 */
	public void addAll(QueryBuilder other);

	/**
	 * Returns true if query word is found in query data structure, false if not
	 * 
	 * @param line String to search
	 * @return true if query word is found in query data structure, false if not
	 */
	public default boolean hasQuery(String line) {
		return !viewSearchResults(line).equals(Collections.emptySet());
	}

	/**
	 * Returns true if query contains SearchResult object, false if not
	 * 
	 * @param line   Query line to search
	 * @param result SearchResult object to search
	 * @return true if query contains SearchResult object, false if not
	 */
	public default boolean hasSearchResult(String line, SearchResult result) {
		return viewSearchResults(line).contains(result);
	}

	/**
	 * @return an unmodifiable view of the query words in query
	 */
	public Set<String> viewQueries();

	/**
	 * Returns an unmodifiable view of the Collection of SearchResults associated
	 * with a word
	 * 
	 * @param line line to stem and search
	 * @return an unmodifiable view of the Collection of SearchResults associated
	 *         with a word
	 */
	public Collection<SearchResult> viewSearchResults(String line);

	/**
	 * Returns the number of queries in the query
	 * 
	 * @return The number of queries in the query
	 */
	public default int numQueries() {
		return viewQueries().size();
	}

	/**
	 * Writes the ArrayList of SearchResults to correct JSON format
	 * 
	 * @param searchResults ArrayList of SearchResults
	 * @param writer        writer to use
	 * @param indent        indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQueryList(ArrayList<SearchResult> searchResults, Writer writer, int indent)
			throws IOException {
		writer.write("[\n");
		var results = searchResults.iterator();
		if (results.hasNext()) {
			results.next().writeJson(writer, indent + 1);

			while (results.hasNext()) {
				writer.write(",\n");
				results.next().writeJson(writer, indent + 1);
			}
			writer.write("\n");
		}
		JsonWriter.writeIndent("]", writer, indent);
	}

	/**
	 * Writes the query to a pretty JSON format
	 * 
	 * @param query  TreeMap structure to get entries
	 * @param writer writer to use
	 * @param indent Indent level
	 * @throws IOException If an IO error occurs
	 */
	public static void writeQuery(TreeMap<String, ArrayList<SearchResult>> query, Writer writer, int indent)
			throws IOException {
		JsonWriter.writeIndent("{\n", writer, indent);
		var iterator = query.entrySet().iterator();
		if (iterator.hasNext()) {
			String line = iterator.next().getKey();
			JsonWriter.writeQuote(line, writer, indent + 1);
			writer.write(": ");
			writeQueryList(query.get(line), writer, indent + 1);

			while (iterator.hasNext()) {
				writer.write(",\n");
				line = iterator.next().getKey();
				JsonWriter.writeQuote(line, writer, indent + 1);
				writer.write(": ");
				writeQueryList(query.get(line), writer, indent + 1);
			}
		}
		writer.write("\n");
		JsonWriter.writeIndent("}", writer, indent);
	}

	/**
	 * Writes query to Json
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO error occurs
	 */
	public void writeQuery(Path path) throws IOException;

}
