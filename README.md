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
<ol>
  <li>
    Building the Inverted Index: The project provides functionalities to build the inverted index by crawling web pages and indexing the collected information.
  </li>
  <li>
    Searching: Users can perform both exact and partial searches through the provided interface.
  </li>
</ol>

<h2>
  Dependencies
</h2>
<ul>
  <li>Java</li>
  <li>Jetty (for the web interface)</li>
</ul>

## Installation

1. **Clone the repository.**

    ```
    git clone https://github.com/yourusername/search-engine.git
    ```

2. **Navigate to the project directory.**

    ```
    cd Search-Engine/src/main/java/edu/usfca/cs272/Driver.java
    ```

3. **Compile the Java files.**

    ```
    javac *.java
    ```

4. **Run the application.**

    ```
    java Driver
    ```

## Contributing
Contributions are welcome! If you have any ideas for improvements or new features, feel free to open an issue or submit a pull request.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
