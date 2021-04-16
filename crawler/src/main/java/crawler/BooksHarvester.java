package crawler;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import static crawler.Configuration.dbDirectory;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class BooksHarvester {

    private static final Gson gson = new Gson();
    private static final Map<String, List<Book>> books = new TreeMap<>();
    private static final List<String> authors = new ArrayList<>();
    private static final List<String> thumbnails = new ArrayList<>();
    private static final String[] genres = new String[] { "history", "philosophy", "art" };

    public static void main(String[] args) throws InterruptedException, IOException {

        final List<Runnable> workers = new ArrayList<>();

        Stream.of(genres).forEach(genre -> {
            final BlockingQueue<Book> queue = new ArrayBlockingQueue<>(100);
            final AtomicBoolean cancel = new AtomicBoolean(false);
            final Runnable producer = new ShelfCrawler(genre, queue, cancel);
            final Runnable consumer = new BookCrawler(books, queue, cancel);
            workers.add(producer);
            workers.add(consumer);
        });
        final ExecutorService executor = Executors.newFixedThreadPool(workers.size());
        workers.forEach(executor::execute);
        executor.shutdown();
        if (!executor.awaitTermination(20L, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        books.keySet().forEach(genre -> {
            final Path path = Paths.get(dbDirectory + genre + ".json");
            final List<Book> booksInGenre = books.get(genre);
            try {
                Files.writeString(path, gson.toJson(booksInGenre), CREATE, WRITE, APPEND);
            } catch (IOException ignored) {
            }
            booksInGenre.forEach(book -> {
                authors.add(book.getAuthor().getPath());
                thumbnails.add(book.getThumbnail());
            });
        });
        final Path thumbnailsPath = Paths.get(dbDirectory + "thumbnails.json");
        final Path authorsPath = Paths.get(dbDirectory + "authors.json");
        Files.writeString(thumbnailsPath, gson.toJson(thumbnails), CREATE, WRITE, APPEND);
        Files.writeString(authorsPath, gson.toJson(authors), CREATE, WRITE, APPEND);
    }
}
