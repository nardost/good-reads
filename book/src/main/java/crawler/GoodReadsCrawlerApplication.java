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
import java.util.stream.IntStream;

import static crawler.Utils.log;
import static crawler.Utils.writeJsonFile;
import static crawler.Parameters.*;

public class GoodReadsCrawlerApplication {

    private static String sourceFile;

    public static void main(String[] args) throws InterruptedException {

        parseProgramArgs(args);

        log("Previously harvested books: " + Harvest.getHarvestedBooks().size());
        log("Previously harvested thumbnails: " + Harvest.getHarvestedThumbnails().size());

        final Set<Book> books = new ConcurrentSkipListSet<>();
        final BlockingQueue<String> idPipe = new ArrayBlockingQueue<>(100);
        final BlockingQueue<String> authorPipe = new ArrayBlockingQueue<>(100);
        final CountDownLatch downloaderDone = new CountDownLatch(numberOfDownloaders);

        final List<Runnable> workers = new ArrayList<>();
        final BookIdPump source = new BookIdPump(sourceFile, idPipe);
        workers.add(source);
        IntStream.range(0, numberOfDownloaders)
                .mapToObj(i -> new BookFilter(idPipe, authorPipe, books, downloaderDone))
                .forEach(workers::add);

        log(workers.size() + " worker threads running");
        final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);
        executor.shutdown();

        log("Waiting for book downloader threads to terminate");
        downloaderDone.await();
        /*
         * Cancel the id pump thread. It might be blocking on the queue.
         */
        source.cancel();
        log("Writing " + books.size() + " books to file...");
        writeJsonFile("", books);
    }

    /**
     * Parses program arguments.
     *   1st argument = arg[0]: the input file
     *   2nd argument = arg[1]: limit to the id stream
     *   3rd argument = arg[2]: maximum tolerable number of http errors (throttling)
     *   4th argument = arg[3]: number of download worker threads
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
        idStreamLimit = Integer.parseInt(args[1]);
        if(args.length == 2) {
            return;
        }
        maxTolerableHttpError = Integer.parseInt(args[2]);
        if(args.length == 3) {
            return;
        }
        numberOfDownloaders = Integer.parseInt(args[3]);
    }
}
