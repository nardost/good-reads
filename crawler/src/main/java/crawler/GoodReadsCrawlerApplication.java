package crawler;

import crawler.book.BookDownloader;
import crawler.book.BookIdSource;

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
import static crawler.book.Parameters.*;

public class GoodReadsCrawlerApplication {

    private static String sourceFile;

    public static void main(String[] args) throws InterruptedException {

        parseProgramArgs(args);

        final Set<Book> books = new ConcurrentSkipListSet<>();
        final BlockingQueue<String> idPipe = new ArrayBlockingQueue<>(100);
        final BlockingQueue<String> authorPipe = new ArrayBlockingQueue<>(100);
        final CountDownLatch downloaderDone = new CountDownLatch(numberOfDownloaders);

        final List<Runnable> workers = new ArrayList<>();
        workers.add(new BookIdSource(sourceFile, idPipe));
        IntStream.range(0, numberOfDownloaders)
                .mapToObj(i -> new BookDownloader(idPipe, authorPipe, books, downloaderDone))
                .forEach(workers::add);

        log(workers.size() + " threads");
        final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);
        executor.shutdown();

        log("Waiting for book downloader threads to terminate");
        downloaderDone.await();
        log("Writing " + books.size() + " books to file...");
        writeJsonFile("", books);
    }

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
