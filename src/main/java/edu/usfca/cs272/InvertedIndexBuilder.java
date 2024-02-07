package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * Builder class for the data structures in InvertedIndex. Goes through each
 * path and updates the data structures with their needed elements.
 * 
 * @author troy
 *
 */
public class InvertedIndexBuilder {
	/**
	 * Returns true if path is a txt/text file, false if not
	 * 
	 * @param path to examine
	 * @return true if the path is a txt file, false if not
	 */
	public static boolean isTxtFile(Path path) {
		String lower = path.toString().toLowerCase();
		return (lower.endsWith(".txt") || lower.endsWith(".text"));
	}

//	public static void processLine(String[] line, SnowballStemmer stemmer, InvertedIndex data) throws IOException {
//		for(String word : line) {
//			var stem = stemmer.stem(word).toString();
//			data.add(stem, location, position++);
//		}
//	}

	/**
	 * Goes through all the words in the path and updates both invertedIndex and
	 * counts with the necessary data
	 * 
	 * @param path Path to use
	 * @param data InvertedIndex data structures
	 * @throws IOException if an IO error occurs
	 */
	public static void processFile(Path path, InvertedIndex data) throws IOException {
		SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
		String location = path.toString();
		int position = 1;
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			while (reader.ready()) {
				for (String word : FileStemmer.parse(reader.readLine())) {
					var stem = stemmer.stem(word).toString();
					data.add(stem, location, position++);
				}
			}
		}
	}

	/**
	 * Traverses through a path if it is a directory. If it not a directory and is a
	 * txt/text file, it will update the data structures
	 * 
	 * @param directory path to check
	 * @param data      Data structures to modify
	 * @throws IOException if an IO error occurs
	 */
	public static void traverseDirectory(Path directory, InvertedIndex data) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path, data);
				} else if (isTxtFile(path)) {
					processFile(path, data);
				}
			}
		}
	}

	/**
	 * Builds invertedIndex and counts data structures
	 * 
	 * @param input Input to use
	 * @param data  Data structures to build
	 * @throws IOException If an IO error occurs
	 */
	public static void build(Path input, InvertedIndex data) throws IOException {
		if (Files.isDirectory(input)) {
			traverseDirectory(input, data);
		} else {
			processFile(input, data);
		}
	}
}
