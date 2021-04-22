package crawler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static crawler.Parameters.BOOKS_DIR;
import static crawler.Parameters.DATA_STORE;
import static crawler.Parameters.THUMBNAILS_DIR;

/**
 * This class retrieves the list of books and
 * thumbnails harvested in previous executions.
 */
public class Harvest {

    private final Set<String> alreadyHarvestedBooks;
    private final Set<String> alreadyHarvestedThumbnails;

    private static final Gson gson = new Gson();

    public Harvest() {
        this.alreadyHarvestedBooks = getHarvestedBooks();
        this.alreadyHarvestedThumbnails = getHarvestedThumbnails();
    }

    public Set<String> getAlreadyHarvestedBooks() {
        return alreadyHarvestedBooks;
    }

    public Set<String> getAlreadyHarvestedThumbnails() {
        return alreadyHarvestedThumbnails;
    }

    public int booksCount() {
        return alreadyHarvestedBooks.size();
    }

    public int thumbnailsCount() {
        return alreadyHarvestedThumbnails.size();
    }

    public boolean containsBook(final String id) {
        return this.alreadyHarvestedBooks.contains(id);
    }

    public boolean containsThumbnail(final String id) {
        return this.alreadyHarvestedThumbnails.contains(id);
    }

    private static Set<String> getHarvestedBooks() {
        final Set<Book> harvestedBooks = new HashSet<>();
        final Path booksDataDir = Path.of(DATA_STORE + File.separator + BOOKS_DIR).toAbsolutePath();
        if(Files.exists(booksDataDir) && Files.isDirectory(booksDataDir)) {
            try {
                Files.walk(booksDataDir).filter(path -> !Files.isDirectory(path)).map(path -> {
                    Set<Book> books = new HashSet<>();
                    try {
                        final BufferedReader bufferedReader = Files.newBufferedReader(path);
                        books = gson.fromJson(bufferedReader, new TypeToken<Set<Book>>() {}.getType());
                    } catch (IOException ignored) {
                    }
                    return books;
                }).forEach(harvestedBooks::addAll);
            } catch (IOException ignored) {
            }
        }
        return harvestedBooks.stream().map(Book::getId).collect(Collectors.toSet());
    }

    private static Set<String> getHarvestedThumbnails() {

        Set<String> harvestedThumbnails = new HashSet<>();
        final Path thumbnailsDataDir = Path.of(DATA_STORE + File.separator + THUMBNAILS_DIR).toAbsolutePath();

        if(Files.exists(thumbnailsDataDir) && Files.isDirectory(thumbnailsDataDir)) {
            try {
                harvestedThumbnails = Files.walk(thumbnailsDataDir)
                        .filter(path -> !Files.isDirectory(path))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .map(fileName -> fileName.replaceAll("\\.\\w{3,}", ""))
                        .collect(Collectors.toSet());
            } catch (IOException ignored) {
            }
        }
        return harvestedThumbnails;
    }
}
