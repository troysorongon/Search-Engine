package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SearchResult Servlet that will get and post the findings from the Inverted
 * Index to the HTML page
 * 
 * @author troy
 *
 */
public class SearchResultServlet extends HttpServlet {

	/** Serial ID (unused) */
	private static final long serialVersionUID = -1951161362670415454L;

	/** The logger to use for this servlet. */
	private static final Logger log = LogManager.getLogger();

	/** Template for starting HTML **/
	private final String headTemplate;

	/** Template for ending HTML **/
	private final String footTemplate;

	/** ArrayList that stores the URLs that match the query */
	private final ArrayList<String> searchResults;

	/** Base path with HTML templates. */
	public static final Path base = Path.of("src", "main", "resources", "html");

	/** The data structure to use for storing messages. */
	private final ThreadSafeInvertedIndex data;

	/**
	 * @param data  InvertedIndex object to use
	 * @param queue WorkQueue for QueryBuilder object to use
	 * @throws IOException If an IO error occurs
	 */
	public SearchResultServlet(ThreadSafeInvertedIndex data, WorkQueue queue) throws IOException {
		this.data = data;
		searchResults = new ArrayList<>();
		headTemplate = Files.readString(base.resolve("bulma-head.html"));
		footTemplate = Files.readString(base.resolve("bulma-foot.html"));
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("{} handling: {}", this.hashCode(), request);

		Map<String, String> values = new HashMap<>();
		values.put("title", "Searchify&#128269");
		values.put("method", "POST");
		values.put("action", request.getServletPath());
		values.put("thread", Thread.currentThread().getName());
		values.put("body", String.join("\n\n", searchResults));

		// generate html from template
		StringSubstitutor replacer = new StringSubstitutor(values);
		String head = replacer.replace(headTemplate);
		String foot = replacer.replace(footTemplate);

		// prepare response
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.println(head);

		synchronized (searchResults) {
			if (searchResults.isEmpty()) {
				out.printf("<p> No Results</p>%n");
			} else {
				for (String link : searchResults) {
					out.println(link);
				}
			}

		}

		out.println(foot);
		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Clears the current ArrayList for new query
		searchResults.clear();

		boolean partialSearch = true;

		var exact = request.getParameter("exact");
		if (exact != null) {
			partialSearch = false;
		}

		var reverse = request.getParameter("reverse");

		String query = request.getParameter("search");
		query = query == null || query.isBlank() ? "" : query;

		query = StringEscapeUtils.escapeHtml4(query);
		Set<String> set = FileStemmer.uniqueStems(query);

		ArrayList<SearchResult> results = data.search(set, partialSearch);
		if (reverse != null) {
			Collections.reverse(results);
		}

		for (SearchResult result : results) {
			searchResults.add(String.format("<a href='%s'>%s</a><br>", result.getPath(), result.getPath()));
		}

		response.sendRedirect(request.getServletPath());
	}

}