package crawler.book;

import crawler.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static crawler.Utils.*;

public class BooksHarvester {

    private static final String[] genres = new String[] { "history", "philosophy", "art" };

    public static void main(String[] args) throws InterruptedException {

        final Map<String, List<Book>> books = new TreeMap<>();
        final List<Runnable> workers = new ArrayList<>();

        Stream.of(genres).forEach(genre -> {
            final BlockingQueue<Book> queue = new ArrayBlockingQueue<>(100);
            final AtomicBoolean cancel = new AtomicBoolean(false);

            final Runnable producer = new BookIdProducer(genre, queue, cancel);
            final Runnable consumer = new BookIdConsumer(books, queue, cancel);

            workers.add(producer);
            workers.add(consumer);
        });
        final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);
        executor.shutdown();
        if (!executor.awaitTermination(20L, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        final List<String> authors = new ArrayList<>();
        final List<String> thumbnails = new ArrayList<>();
        books.keySet().forEach(genre -> {
            final List<Book> booksInGenre = books.get(genre);
            booksInGenre.forEach(book -> {
                authors.add(book.getAuthor().getPath());
                thumbnails.add(book.getThumbnail());
            });
            writeJsonFile(genre, booksInGenre);
        });

        writeJsonFile("_thumbnails", thumbnails);
        writeJsonFile("_authors", authors);
    }
}
