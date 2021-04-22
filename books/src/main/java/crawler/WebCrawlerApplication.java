package crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static crawler.Utils.log;
import static crawler.Utils.writeJsonFile;
import static crawler.Parameters.*;

public class WebCrawlerApplication {

    private static String sourceFile;

    public static void main(String[] args) throws InterruptedException {

        parseProgramArgs(args);

        final Set<Book> books = new ConcurrentSkipListSet<>();
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

        int round = 1;
        do {
            if(maxNumberOfLoops > 0) {
                log("Round " + round + " of " + maxNumberOfLoops);
            }

            final Harvest harvest = new Harvest();
            log("Previously harvested books:      " + harvest.booksCount());
            log("Previously harvested thumbnails: " + harvest.thumbnailsCount());
            if(harvest.booksCount() >= downloadGoal) {
                log("Download goal has been reached.");
                break;
            }

            final CountDownLatch doneSignal = new CountDownLatch(numberOfWorkerThreads);

            final List<Runnable> workers = new ArrayList<>();
            final BookIdSource source = new BookIdSource(sourceFile, queue, harvest);
            workers.add(source);
            IntStream.range(0, numberOfWorkerThreads)
                    .mapToObj(i -> new BookInfoCollector(queue, books, doneSignal))
                    .forEach(workers::add);
            final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
            workers.forEach(executor::execute);
            log(workers.size() + " worker threads running");
            executor.shutdown();

            log("Waiting for book downloader threads to terminate...");
            doneSignal.await();
            log("All book downloader threads terminated...");

            // Cancel the id pump thread. It might be blocking on the queue.
            source.cancel();

            if(!books.isEmpty()) {
                log("Writing " + books.size() + " books to file...");
                writeJsonFile("", books);
            }

            // Reuse collections instead of creating new on every round.
            books.clear();
            queue.clear();

            if(round + 1 < maxNumberOfLoops) {

                // Random duration of time to sleep
                final long delay = ThreadLocalRandom.current()
                        .longs(minSleepTimeInSeconds, maxSleepTimeInSeconds)
                        .findFirst()
                        .orElse(600L);
                log("Sleeping for " + delay + " seconds ...");
                TimeUnit.SECONDS.sleep(delay);
            }
        } while (round++ < maxNumberOfLoops);
        log("Program is terminating ...");
    }

    /**
     * Parses program arguments.
     *   1st argument = arg[0]: the input file
     *   2nd argument = arg[1]: maximum number of times the program loops
     *   3rd argument = arg[2]: number of download worker threads
     *   4th argument = arg[3]: maximum tolerable number of http errors (throttling)
     *   5th argument = arg[4]: limit to the id stream
     *
     *
     * @param args program argument list
     */
    private static void parseProgramArgs(final String[] args) {

        if(args.length == 0) {
            throw new RuntimeException("Input file is not provided.");
        }
        sourceFile = args[0];

        if(args.length == 1) {
            return;
        }
        maxNumberOfLoops = parseWithFallback(args[1], maxNumberOfLoops).intValue();

        if(args.length == 2) {
            return;
        }
        numberOfWorkerThreads = parseWithFallback(args[2], numberOfWorkerThreads).intValue();

        if(args.length == 3) {
            return;
        }
        maxTolerableHttpError = parseWithFallback(args[3], maxTolerableHttpError).intValue();

        if(args.length == 4) {
            return;
        }
        streamLimit = parseWithFallback(args[4], streamLimit).intValue();

    }

    private static Number parseWithFallback(final String arg, final Number fallback) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
