package crawler;

public class Parameters {

    /**
     * Maximum number of ids pumped into the pipeline
     */
    public static int idStreamLimit = 200;
    /**
     * Number of scraper worker threads
     */
    public static int numberOfWorkerThreads = 3;
    /**
     * Maximum number of http errors after which program suspects throttling and aborts
     */
    public static int maxTolerableHttpError = 10;
    /**
     * Maximum number of books to scrape
     */
    public static int downloadGoal = 1000;
    /**
     * Maximum number times the program loops
     */
    public static int maxNumberOfLoops = 1;
    /**
     * The minimum duration of time in seconds program sleeps between rounds
     */
    public static long minSleepTimeInSeconds = 120L;
    /**
     * The maximum duration of time in seconds program sleeps between rounds
     */
    public static long maxSleepTimeInSeconds = 300L;

    /**
     * Id value that signals the end of the id stream to Runnable consumers downstream
     */
    public static final String POISON_PILL = "0";
    /**
     * Directory under which scraped data are stored
     */
    public static final String DATA_STORE = "DATA";
    /**
     * Sub-directory of ${DAT_STORE} for JSON files of books
     */
    public static final String BOOKS_DIR = "books";
    /**
     * Sub-directory of ${DAT_STORE} for thumbnails
     */
    public static final String THUMBNAILS_DIR = "thumbnails";
}
