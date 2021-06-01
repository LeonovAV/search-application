# In-memory text search application

This project is created for quick indexing and simple text search of documents on the file system. 
Application builds an index concurrently for all files based on the given folder (files in sub-folders are also included).
Text index is stored in memory and uses trigrams in order to reduce memory consumption and do not decrease performance for 
search operations.

#### Project features

This project allows one:
 * indexing chosen folder and its sub-folders;
 * perform search queries (full-text search support);
 * track changes for chosen folder and sub-folders;
 * track changes for files in chosen folder;
 * update search results according to file system changes.

#### Project structure

This project consists of several modules:
 * app - main module, which initializes all components and run GUI;
 * document-indexer - module responsible for keeping in-memory index up to date;
 * document-searcher - module responsible for performing search queries and keep search results up to date;
 * document-index - module responsible for managing in-memory index (add, update, remove operations for trigrams).
  
#### How to run an application

To run an application with Gradle execute the following command in the root project folder: 
```
./gradlew run
```
It is possible to create an executable jar file and run it: 
```
./gradlew clean build
java -jar app/build/libs/app-1.0-SNAPSHOT.jar
```
As an alternative - use your favourite IDE to run the project.

#### Implementation notes

Main components of the application are implemented using interfaces (for example, document index, document store 
searcher or indexer). So, it is possible to change the implementation to your own or extend/change the existing one.

#### Limitations

There are several limitations in current implementation:
 * supports only txt, java, kt, html, css, js file extensions (could be easily changed in source code);
 * number of search results is limited to 100 (also could be changed);
 * found positions are displayed in the log file.

#### Improvements and TODOs

There are some TODOs or improvements, that could be applied:
 * show positions in the found file (or highlight found words);
 * write more tests.
