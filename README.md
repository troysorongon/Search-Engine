Search Engine
=================================================

This Java-based Search Engine project is designed to efficiently build and search through an inverted index data structure. It offers functionalities for both exact search and partial search, providing users with a comprehensive search experience.

<h2>Key Features</h2>

<ul>
  <li>
    Inverted Index: The project builds and utilizes an inverted index data structure for efficient searching.
  </li>
  <li>
    Exact and Partial Search: It supports both exact search and partial search operations, enhancing flexibility in querying.
  </li>
  <li>
    Multithreading: Custom-made read/write lock objects and a work queue are employed to manage worker threads, enabling efficient multithreading capabilities.
  </li>
  <li>
    Web Crawler: The project includes a web crawler component that recursively scans and collects information from web pages, aiding in the construction of the inverted index from a seed URL.
  </li>
  <li>
    Web Interface: Featuring a web interface, users can input multi-word queries through an HTML form. The system returns sorted partial search results dynamically generated as a web page. This functionality is implemented using embedded Jetty and servlets, providing a user-friendly search experience.
  </li>
</ul>

<h2>Usage</h2>
