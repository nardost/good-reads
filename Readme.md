### Running the Crawler

```markdown
### Building the Uber Jar (mocOS/Linux)
$ ./gradlew :book:fatJar
(In Windows, use the batch file gradlew.bat to build)
```
The executable jar file will be in book/build/libs. You can move this jar file anywhere in your file system and run it.



```markdown
### Running the Executable (macOS/Linux)
$ java -jar book-1.0-SNAPSHOT-uber.jar /path/to/id_source_file.json id_stream_limit max_forbidden n_threads

Where,
   /path/to/id_source_file.json is the path to the id source file assigned to you.
   id_stream_limit (defaults to 200) is the maximum number of book ids you want to pump into the pipeline
   max_forbidden (defaults to 10) is the maximum number of forbidden responses (403) before aborting (when the remote throttles...)
   n_threads (defaults to 3) is the number of book scraper (worker) threads
```
The only required program argument is the id source JSON file. You can use the default values for the other 3 arguments, or you may supply them to experiment...

Example:

```markdown
$ java -jar book-1.0-SNAPSHOT-uber.jar ./nardos_ffd6b834c7b94385a12dad1b03b744fb.json 1000 10 5
```

The program will create a directory structure under the current directory where it saves the thumbnails and the json files.

Example:

```markdown
.
├── DATA
│   ├── books
│   │   └── _69406cb92bf7431d84304346cb3683ee.json
│   └── thumbnails
│       ├── 1041353.jpg
│       ├── 15999911.jpg
│       ├── 18007535.jpg
│       ├── 198095.jpg
│       ├── 27163019.jpg
│       ├── 29151803.jpg
│       ├── 34912895.jpg
│       ├── 39835415.jpg
│       ├── 40593233.jpg
│       ├── 449261.jpg
│       ├── 52422311.jpg
│       ├── 8038799.jpg
│       └── 8526377.jpg
├── book-1.0-SNAPSHOT-uber.jar
└── nardos_ffd6b834c7b94385a12dad1b03b744fb.json
```

