package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multi-threaded implementation of InvertedIndexBuilder that uses a static
 * inner class to build the InvertedIndex data structure path by path
 * 
 * @author troy
 *
 */
public class MultiThreadedInvertedIndexBuilder {

	/**
	 * Logger object for debugging purposes
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Traverses through a path if it is a directory. If it not a directory and is a
	 * txt/text file, it will update the data structures
	 * 
	 * @param directory path to check
	 * @param data      Data structures to modify
	 * @param queue     current queue
	 * @throws IOException if an IO error occurs
	 */
	public static void traverseDirectory(Path directory, ThreadSafeInvertedIndex data, WorkQueue queue)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
			for (Path path : listing) {
				if (Files.isDirectory(path)) {
					traverseDirectory(path, data, queue);
				} else if (InvertedIndexBuilder.isTxtFile(path)) {
					queue.execute(new Task(path, data));
				}
			}
		}
	}

	/**
	 * @param path  Path to process
	 * @param data  Data structure to update
	 * @param queue Current WorkQueue
	 * @throws IOException If an IO error occurs
	 */
	public static void build(Path path, ThreadSafeInvertedIndex data, WorkQueue queue) throws IOException {

		if (Files.isDirectory(path)) {
			traverseDirectory(path, data, queue);
		} else {
			queue.execute(new Task(path, data));
		}

		queue.finish();
	}

	/**
	 * @author troy
	 *
	 */
	private static class Task implements Runnable {

		/**
		 * data structure to set
		 */
		private final ThreadSafeInvertedIndex data;

		/**
		 * Path to set
		 */
		private final Path path;

		/**
		 * @param data Data object to ubdate
		 * @param path Path to search
		 */
		private Task(Path path, ThreadSafeInvertedIndex data) {
			this.data = data;
			this.path = path;
			log.debug("In Constructor. Path: " + this.path);
		}

		@Override
		public void run() {
			log.debug("Starting run() method on path: " + path);
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.processFile(path, local);
				data.addAll(local);
			} catch (IOException e) {
				log.catching(Level.ERROR, e);
				throw new UncheckedIOException(e);
			}
			log.debug("Completed run() method on: " + path);
		}

	}
}
