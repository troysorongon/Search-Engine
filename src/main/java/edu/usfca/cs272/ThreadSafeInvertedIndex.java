package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Inverted Index class that creates the needed data structures to store counts
 * and the inverted index for each word. This class also contains helpful
 * methods for each data structure.
 * 
 * @author Troy Sorongon
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/**
	 * The lock used to protect concurrent access to the invertedIndex and counts
	 */
	private final MultiReaderLock lock;

	/**
	 * Initializes new TreeMaps for invertedIndex and pairs
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new MultiReaderLock();
	}

	/**
	 * Performs a partial search on invertedIndex data structure based on the
	 * strings in queries
	 * 
	 * @param queries The set of words to search
	 * @return ArrayList of TreeMaps hold count, score, and where values
	 */
	@Override
	public ArrayList<edu.usfca.cs272.InvertedIndex.SearchResult> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a partial search on invertedIndex data structure based on the
	 * strings in queries
	 * 
	 * @param queries The set of words to search
	 * @return ArrayList of TreeMaps hold count, score, and where values
	 */
	@Override
	public ArrayList<edu.usfca.cs272.InvertedIndex.SearchResult> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
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
	@Override
	public void add(String stem, String path, int position) {
		lock.writeLock().lock();
		try {
			super.add(stem, path, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of counts.
	 * 
	 * @return an unmodifiable view of counts.
	 */
	@Override
	public Map<String, Integer> viewCounts() {
		lock.readLock().lock();

		try {
			return super.viewCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of the locations in counts
	 * 
	 * @return an unmodifiable view of the locations in counts
	 */
	@Override
	public Set<String> viewLocations() {
		lock.readLock().lock();

		try {
			return super.viewLocations();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of the indices found for a word in a file
	 * 
	 * @param word Word to search
	 * @param file File to search
	 * @return an unmodifiable view of the indices found for a word in a file
	 */
	@Override
	public Set<Integer> viewIndices(String word, String file) {
		lock.readLock().lock();

		try {
			return super.viewIndices(word, file);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of the paths a word is found in invertedIndex
	 * 
	 * @param word word to search
	 * @return an unmodifiable view of the paths a word is found in invertedIndex
	 */
	@Override
	public Set<String> viewPaths(String word) {
		lock.readLock().lock();

		try {
			return super.viewPaths(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable view of all the stems in InvertedIndex
	 * 
	 * @return an unmodifiable view of all the stems in InvertedIndex
	 */
	@Override
	public Set<String> viewWords() {
		lock.readLock().lock();

		try {
			return super.viewWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns true if path is a key, false if not
	 * 
	 * @param path Path to check
	 * @return true if path is a key, false if not
	 */
	@Override
	public boolean hasPath(String path) {
		lock.readLock().lock();

		try {
			return super.hasPath(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns true if a stem is a key, false if not
	 * 
	 * @param stem Stem to check
	 * @return Returns true if a stem is a key, false if not
	 */
	@Override
	public boolean hasWord(String stem) {
		lock.readLock().lock();

		try {
			return super.hasWord(stem);
		} finally {
			lock.readLock().unlock();
		}
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
	@Override
	public boolean hasPath(String word, String path) {
		lock.readLock().lock();

		try {
			return super.hasPath(word, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns true if position is found in the word's associated path, false if not
	 * 
	 * @param word     Word to check
	 * @param path     Path to check
	 * @param position Position to check
	 * @return true if position is found in the word's associated path, false if not
	 */
	@Override
	public boolean hasPosition(String word, String path, int position) {
		lock.readLock().lock();

		try {
			return super.hasPosition(word, path, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns size of pairs
	 * 
	 * @return size of pairs
	 */
	@Override
	public int countsSize() {
		lock.readLock().lock();

		try {
			return super.countsSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the word count for the path
	 * 
	 * @param path Path to search
	 * @return the word count for the path
	 */
	@Override
	public int wordCount(String path) {
		lock.readLock().lock();

		try {
			return super.wordCount(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns size of invertedIndex
	 * 
	 * @return size of invertedIndex
	 */
	@Override
	public int numWords() {
		lock.readLock().lock();

		try {
			return super.numWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of locations the word is present in
	 * 
	 * @param word Word to search
	 * @return the number of locations the word is present in
	 */
	@Override
	public int numLocations(String word) {
		lock.readLock().lock();

		try {
			return super.numLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of positions the word is found in the path
	 * 
	 * @param word Word to search for
	 * @param path Path to search for
	 * @return the number of positions the word is found in the path
	 */
	@Override
	public int numPositions(String word, String path) {
		lock.readLock().lock();

		try {
			return super.numPositions(word, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();

		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Calls JsonWriter method to write invertedIndex to JSON
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO Error occurs
	 */
	@Override
	public void writeIndex(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.writeIndex(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Calls JsonWriter method to write counts to JSON
	 * 
	 * @param path Path to write to
	 * @throws IOException If an IO Error occurs
	 */
	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.writeCounts(path);
		} finally {
			lock.readLock().unlock();
		}
	}
}
