package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Inverted Index class that creates the needed data structures to store counts
 * and the inverted index for each word. This class also contains helpful
 * methods for each data structure.
 * 
 * @author troy
 *
 */
public class InvertedIndex {

	/**
	 * Nested TreeMap data structure that represents what path and what position a
	 * stem is found in
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * TreeMap data structure to hold the path and it's word count
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * Initializes new TreeMaps for invertedIndex and pairs
	 */
	public InvertedIndex() {
		invertedIndex = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		counts = new TreeMap<String, Integer>();
	}

	/**
	 * Performs either exact or partial search
	 * 
	 * @param queries The set of words to search
	 * @param partial boolean that decides type of search
	 * @return ArrayList of TreeMaps hold count, score, and where values
	 */
	public ArrayList<SearchResult> search(Set<String> queries, boolean partial) {
		return partial ? partialSearch(queries) : exactSearch(queries);
	}

	/**
	 * Creates a SearchResult object based on lookup Hashmap data then gets added to
	 * results ArrayList
	 * 
	 * @param query         Query to search in lookup HashMap
	 * @param invertedIndex TreeMap to get data from
	 * @param lookup        Local HashMap that stores data that is checked when
	 *                      going through all queries
	 * @param results       ArrayList of type SearchResult object to return
	 */
	private void addSearchResult(String query, TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex,
			HashMap<String, SearchResult> lookup, ArrayList<SearchResult> results) {
		for (Entry<String, TreeSet<Integer>> entry : invertedIndex.get(query).entrySet()) {
			SearchResult result = lookup.get(entry.getKey());
			if (result == null) {
				result = new SearchResult(entry.getKey());
				lookup.put(entry.getKey(), result);
				results.add(result);
			}
			result.update(query);
		}
	}

	/**
	 * Performs a partial search on invertedIndex data structure based on the
	 * strings in queries
	 * 
	 * @param queries The set of words to search
	 * @return ArrayList of SearchResults hold count, score, and where values
	 */
	public ArrayList<SearchResult> exactSearch(Set<String> queries) {
		HashMap<String, SearchResult> lookup = new HashMap<>();
		ArrayList<SearchResult> results = new ArrayList<>();
		for (String query : queries) {
			if (invertedIndex.containsKey(query)) {
				addSearchResult(query, invertedIndex, lookup, results);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * Performs a partial search on invertedIndex data structure based on the
	 * strings in queries
	 * 
	 * @param queries The set of words to search
	 * @return ArrayList of SearchResults hold count, score, and where values
	 */
	public ArrayList<SearchResult> partialSearch(Set<String> queries) {
		HashMap<String, SearchResult> lookup = new HashMap<>();
		ArrayList<SearchResult> results = new ArrayList<>();
		for (String query : queries) {
			for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : invertedIndex.tailMap(query).entrySet()) {
				String filtered = entry.getKey();
				if (!filtered.startsWith(query)) {
					break;
				}
				addSearchResult(filtered, invertedIndex, lookup, results);
			}
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Performs a partial search on invertedIndex data structure based on the string
	 * query
	 * 
	 * @param query String to search
	 * @return ArrayList of SearchResults hold count, score, and where values
	 */
	public ArrayList<SearchResult> partialSearch(String query) {
		HashMap<String, SearchResult> lookup = new HashMap<>();
		ArrayList<SearchResult> results = new ArrayList<>();
		for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : invertedIndex.tailMap(query).entrySet()) {
			String filtered = entry.getKey();
			if (!filtered.startsWith(query)) {
				break;
			}
			addSearchResult(filtered, invertedIndex, lookup, results);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Adds stem, it's path, and it's position in file to the invertedIndex data
	 * structure. Will create a data structure to hold information for each
	 * associated key if not already present
	 * 
	 * @param stem     Stem that represents key in data structure
	 * @param path     the Path the stem is found in
	 * @param position index of the stem in path
	 */
	public void add(String stem, String path, int position) {
		invertedIndex.putIfAbsent(stem, new TreeMap<>());
		invertedIndex.get(stem).putIfAbsent(path, new TreeSet<>());
		invertedIndex.get(stem).get(path).add(position);

		if (counts.getOrDefault(path, 0) < position) {
			counts.put(path, position);
		}
	}

	/**
	 * Adds the data in another InvertedIndex object to this InvertedIndex object
	 * 
	 * @param other InvertedIndex object to use
	 */
	public void addAll(InvertedIndex other) {
		for (var wordEntry : other.invertedIndex.entrySet()) {
			String otherWord = wordEntry.getKey();
			TreeMap<String, TreeSet<Integer>> otherLocations = wordEntry.getValue();
			TreeMap<String, TreeSet<Integer>> thisLocations = this.invertedIndex.get(otherWord);

			if (thisLocations == null) {
				// Sets initial String-TreeMap<>()
				this.invertedIndex.put(otherWord, otherLocations);
			} else {
				for (var otherEntry : otherLocations.entrySet()) {
					if (thisLocations.containsKey(otherEntry.getKey())) {
						// Updates locations if path is already in invertedIndex
						this.invertedIndex.get(otherWord).get(otherEntry.getKey())
								.addAll(otherLocations.get(otherEntry.getKey()));
					} else {
						// Adds String-TreeSet<>() value to String key in invertedIndex
						this.invertedIndex.get(otherWord).put(otherEntry.getKey(),
								otherLocations.get(otherEntry.getKey()));
					}
				}
			}

		}

		for (var countEntry : other.counts.entrySet()) {
			String otherPath = countEntry.getKey();
			int otherCount = countEntry.getValue();
			if (this.counts.getOrDefault(otherPath, 0) < otherCount) {
				this.counts.put(otherPath, otherCount);
			}
		}
	}

	/**
	 * Returns an unmodifiable view of counts.
	 * 
	 * @return an unmodifiable view of counts.
	 */
	public Map<String, Integer> viewCounts() {
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Returns an unmodifiable view of the locations in counts
	 * 
	 * @return an unmodifiable view of the locations in counts
	 */
	public Set<String> viewLocations() {
		return Collections.unmodifiableSet(counts.keySet());
	}

	/**
	 * Returns an unmodifiable view of the indices found for a word in a file
	 * 
	 * @param word Word to search
	 * @param file File to search
	 * @return an unmodifiable view of the indices found for a word in a file
	 */
	public Set<Integer> viewIndices(String word, String file) {
		TreeSet<Integer> indices = invertedIndex.get(word).get(file);
		if (indices != null) {
			return Collections.unmodifiableSet(indices);
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the paths a word is found in invertedIndex
	 * 
	 * @param word word to search
	 * @return an unmodifiable view of the paths a word is found in invertedIndex
	 */
	public Set<String> viewPaths(String word) {
		TreeMap<String, TreeSet<Integer>> files = invertedIndex.get(word);
		if (files != null) {
			return Collections.unmodifiableSet(files.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of all the stems in InvertedIndex
	 * 
	 * @return an unmodifiable view of all the stems in InvertedIndex
	 */
	public Set<String> viewWords() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * Returns true if path is a key, false if not
	 * 
	 * @param path Path to check
	 * @return true if path is a key, false if not
	 */
	public boolean hasPath(String path) {
		return counts.containsKey(path);
	}

	/**
	 * Returns true if a stem is a key, false if not
	 * 
	 * @param stem Stem to check
	 * @return Returns true if a stem is a key, false if not
	 */
	public boolean hasWord(String stem) {
		return invertedIndex.containsKey(stem);
	}

	/**
	 * Returns true if path is found in the collection of paths for a stem, false if
	 * not
	 * 
	 * @param word Word to check
	 * @param path Path to check
	 * @return true if path is found in the collection of paths for a stem, false if
	 *         not
	 */
	public boolean hasPath(String word, String path) {
		return hasWord(word) && invertedIndex.get(word).containsKey(path);
	}

	/**
	 * Returns true if position is found in the word's associated path, false if not
	 * 
	 * @param word     Word to check
	 * @param path     Path to check
	 * @param position Position to check
	 * @return true if position is found in the word's associated path, false if not
	 */
	public boolean hasPosition(String word, String path, int position) {
		return hasPath(word, path) && invertedIndex.get(word).get(path).contains(position);
	}

	/**
	 * Returns size of pairs
	 * 
	 * @return size of pairs
	 */
	public int countsSize() {
		return counts.size();
	}

	/**
	 * Returns the word count for the path
	 * 
	 * @param path Path to search
	 * @return the word count for the path
	 */
	public int wordCount(String path) {
		return counts.get(path);
	}

	/**
	 * Returns size of invertedIndex
	 * 
	 * @return size of invertedIndex
	 */
	public int numWords() {
		return invertedIndex.size();
	}

	/**
	 * Returns the number of locations the word is present in
	 * 
	 * @param word Word to search
	 * @return the number of locations the word is present in
	 */
	public int numLocations(String word) {
		return viewPaths(word).size();
	}

	/**
	 * Returns the number of positions the word is found in the path
	 * 
	 * @param word Word to search for
	 * @param path Path to search for
	 * @return the number of positions the word is found in the path
	 */
	public int numPositions(String word, String path) {
		return viewIndices(word, path).size();
	}

	@Override
	public String toString() {
		return "Counts: " + counts.toString() + "\n" + "invertedIndex: " + invertedIndex.toString();
	}

	/**
	 * Calls JsonWriter method to write invertedIndex to JSON
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO Error occurs
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.writeInvertedIndex(invertedIndex, path);
	}

	/**
	 * Calls JsonWriter method to write counts to JSON
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO Error occurs
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeObject(counts, path);
	}

	/**
	 * Public non-static inner class for SearchResult object that stores the
	 * neccessary for search
	 * 
	 * @author troy
	 *
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/** The number of occurrences of word in path */
		private int count;

		/** percentage of the occurrence of the word in path */
		private double score;

		/** The path the word is found in */
		private final String path;

		/**
		 * Constructor of object that sets the count, score, and path
		 * 
		 * @param path path to set
		 */
		public SearchResult(String path) {
			this.count = 0;
			this.score = 0;
			this.path = path;
		}

		/**
		 * Helper method to update the count and score of the query
		 * 
		 * @param query query to update its values
		 */
		private void update(String query) {
			this.count += invertedIndex.get(query).get(path).size();
			this.score = (double) this.count / counts.get(path);
		}

		@Override
		public String toString() {
			return "Count: " + count + "\nScore: " + score + "\nPath: " + path;
		}

		@Override
		public int compareTo(SearchResult other) {
			if (Double.compare(other.score, this.score) != 0) {
				return Double.compare(other.score, this.score);
			} else if (Integer.compare(other.count, this.count) != 0) {
				return Integer.compare(other.count, this.count);
			} else {
				return this.path.compareToIgnoreCase(other.path);
			}
		}

		/**
		 * @return count (int)
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * @return score (double)
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * @return path (String)
		 */
		public String getPath() {
			return this.path;
		}

		/**
		 * Writes the SearchResult object in correct JSON format
		 * 
		 * @param writer the writer to use
		 * @param indent indent level
		 * @throws IOException if an IO error occurs
		 */
		public void writeJson(Writer writer, int indent) throws IOException {
			JsonWriter.writeIndent("{\n", writer, indent);
			DecimalFormat df = new DecimalFormat("0.00000000");
			JsonWriter.writeQuote("count", writer, indent + 1);
			writer.write(": " + count + ",\n");
			JsonWriter.writeQuote("score", writer, indent + 1);
			writer.write(": " + df.format(score) + ",\n");
			JsonWriter.writeQuote("where", writer, indent + 1);
			writer.write(": " + '"' + path + '"' + "\n");
			JsonWriter.writeIndent("}", writer, indent);
		}
	}
}
