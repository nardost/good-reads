### System Diagram

![System Diagram](/docs/images/system.png)

### Running the Crawler

```markdown
### Building the Uber Jar (mocOS/Linux)

$ ./gradlew :books:uberJar
(In Windows, use the batch file gradlew.bat to build)
```
The executable jar file will be in book/build/libs. You can move this jar file anywhere in your file system and run it.

```markdown
### Running the Executable (macOS/Linux)

$ java -jar good-reads-crawler-1.0.3.jar /path/to/id_source_file.json [loops] [n_threads] [max_forbidden] [ id_stream_limit]
```
### Program Argument List (in that order)

Position in Args | Argument | Description | Required/Optional 
---- | --- | --- | ---
1st | ```/path/to/id_source_file.json``` | path to the id source file assigned to you. | REQUIRED 
2nd | ```loops``` | number of program loops | OPTIONAL
3rd | ```n_threads``` | number of book scraper (worker) threads | OPTIONAL
4th | ```max_forbidden``` | maximum number of forbidden responses (403) before aborting (when the remote throttles...) | OPTIONAL
5th | ```id_stream_limit``` | maximum number of book ids you want to pump into the pipeline | OPTIONAL

The only required program argument is the id source JSON file.

### Examples:

```markdown
- program uses default parameters
$ java -jar good-reads-crawler-1.0.3.jar ./nardos_ffd6b834c7b94385a12dad1b03b744fb.json

- program loops for a maximum of 100 rounds.
$ java -jar good-reads-crawler-1.0.3.jar ./nardos_ffd6b834c7b94385a12dad1b03b744fb.json 100

- Program loops for a maximum of 100 rounds and 3 worker threads are spawned.
  $ java -jar good-reads-crawler-1.0.3.jar ./nardos_ffd6b834c7b94385a12dad1b03b744fb.json 100 3
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
├── good-reads-crawler-1.0.0.jar
└── nardos_ffd6b834c7b94385a12dad1b03b744fb.json
```
## KEEP THE DOWNLOADED FILES WHERE THEY ARE! 

**THE PROGRAM REMEMBERS WHAT IT HAS DOWNLOADED IN EARLIER EXECUTIONS AND WILL NOT TRY TO RE-DOWNLOAD BASED ON WHAT IT SEES IN THE DATA DIRECTORIES.** 