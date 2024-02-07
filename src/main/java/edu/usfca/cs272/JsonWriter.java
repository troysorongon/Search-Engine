package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");
		Iterator<? extends Number> iterator = elements.iterator();
		if (iterator.hasNext()) {
			writer.write("\n");
			writeIndent(iterator.next().toString(), writer, indent + 1);

			while (iterator.hasNext()) {
				writer.write(",\n");
				writeIndent(iterator.next().toString(), writer, indent + 1);
			}
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper method for writeObject to write entry for the first or other elements
	 * 
	 * @param key    Key to use
	 * @param num    Number to use
	 * @param writer Writer to use
	 * @param indent Indents to use
	 * @param first  If it's the first element or not
	 * @throws IOException if an IO Error occurs
	 */
	public static void writeObjectEntry(String key, Number num, Writer writer, int indent, boolean first)
			throws IOException {
		if (first) {
			writer.write("\n");
		} else {
			writer.write(",\n");
		}
		writeQuote(key, writer, indent + 1);
		writer.write(": ");
		writer.write(num.toString());
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			String key = iterator.next().getKey();
			var num = elements.get(key);
			writeObjectEntry(key, num, writer, indent, true);

			while (iterator.hasNext()) {
				key = iterator.next().getKey();
				num = elements.get(key);
				writeObjectEntry(key, num, writer, indent, false);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper method for writeObjectArrays to write entry for the first or other
	 * elements
	 * 
	 * @param key    Key to use
	 * @param arr    Collection to use
	 * @param writer Writer to use
	 * @param indent Indent to use
	 * @param first  If it's the first element or not
	 * @throws IOException if an IO Exception occurs
	 */
	public static void writeObjectArraysEntry(String key, Collection<? extends Number> arr, Writer writer, int indent,
			boolean first) throws IOException {
		if (first) {
			writer.write("\n");
		} else {
			writer.write(",\n");
		}
		writeQuote(key, writer, indent + 1);
		writer.write(": ");
		writeArray(arr, writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			String key = iterator.next().getKey();
			var arr = elements.get(key);
			writeObjectArraysEntry(key, arr, writer, indent, true);

			while (iterator.hasNext()) {
				key = iterator.next().getKey();
				arr = elements.get(key);
				writeObjectArraysEntry(key, arr, writer, indent, false);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper method for writeArrayObjects to write entry for the first or other
	 * elements
	 * 
	 * @param entry  Entry to use
	 * @param writer Writer to use
	 * @param indent Indent to use
	 * @param first  If it's the first element or not
	 * @throws IOException If an IO error occurs
	 */
	public static void writeArrayObjectsEntry(Map<String, ? extends Number> entry, Writer writer, int indent,
			boolean first) throws IOException {
		if (first) {
			writer.write("\n");
		} else {
			writer.write(",\n");
		}
		writeIndent(writer, 1);
		writeObject(entry, writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			var obj = iterator.next();
			writeArrayObjectsEntry(obj, writer, indent, true);

			while (iterator.hasNext()) {
				obj = iterator.next();
				writeArrayObjectsEntry(obj, writer, indent, false);
			}
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper method for writeArrayObjects to write entry for the first or other
	 * elements
	 * 
	 * @param word   Word to use
	 * @param nested Nested data structure to use
	 * @param writer Writer to use
	 * @param indent Indent to use
	 * @param first  If it's the first element or not
	 * @throws IOException If an IO error occurs
	 */
	public static void writeInvertedIndexEntry(String word, Map<String, ? extends Collection<? extends Number>> nested,
			Writer writer, int indent, boolean first) throws IOException {
		if (first) {
			writer.write("\n");
		} else {
			writeIndent(",\n", writer, indent);
		}
		writeQuote(word, writer, indent + 1);
		writer.write(": ");
		writeObjectArrays(nested, writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested data structures. The
	 * generic notation used allows this method to be used for any type of Map with
	 * any type of nested map of String keys with any type of nested collection of
	 * Number indices.
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first curly brace is not
	 *                 indented, inner elements are indented by one after the
	 *                 previous elements, and the last curly brace is indented at
	 *                 the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer,
			int indent) throws IOException {
		writeIndent("{", writer, indent);
		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var word = iterator.next().getKey();
			var nested = elements.get(word);
			writeInvertedIndexEntry(word, nested, writer, indent, true);

			while (iterator.hasNext()) {
				word = iterator.next().getKey();
				nested = elements.get(word);
				writeInvertedIndexEntry(word, nested, writer, indent, false);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 * 
	 * @param elements the elements to write
	 * @param path     the path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeInvertedIndex(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 * 
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeInvertedIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeInvertedIndex(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
