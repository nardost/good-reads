package crawler.legacy;

import crawler.Author;
import crawler.Book;
import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static crawler.Utils.*;

@AllArgsConstructor
public class BookIdConsumer implements Runnable {

    private final Map<String, List<Book>> books;
    private final BlockingQueue<Book> queue;
    private final AtomicBoolean cancel;

    /**
     * Max number of forbidden requests before aborting.
     */
    private static final int MAX_FORBIDDEN = 5;

    @Override
    public void run() {
        int forbiddenCount = 0;
        while(!cancel.get()) {
            try {
                final Book book = queue.take();
                log("Trying to get details for book " + book.getId());
                final Connection connection = Jsoup.connect("https://www.goodreads.com/book/show/" + book.getId());
                connection.ignoreHttpErrors(true);
                if (connection.execute().statusCode() == 200) {
                    final Document document = connection.get();
                    if (Objects.nonNull(document)) {
                        document.select("div#topcol").forEach(e -> {
                            book.setTitle(e.select("div#topcol h1#bookTitle").text());
                            book.setThumbnail(e.select("div#topcol div#imagecol div.bookCoverPrimary img#coverImage").attr("src"));
                            final String dataTextId = e.select("div#topcol div#description a").attr("data-text-id");
                            book.setBlurb(e.select("div#topcol div#description span#freeText" + dataTextId).text());
                        });
                        /*final String key = book.getGenres().isEmpty() ? "unknown-genre" : book.getGenres().get(0);
                        if(Objects.nonNull(books.get(key))) {
                            books.get(key).add(book);
                        } else {
                            final List<Book> booksInGenre = new ArrayList<>();
                            booksInGenre.add(book);
                            books.put(key, booksInGenre);
                        }*/
                        log("Got details for book " + book.getId());
                    }
                } else {
                    cancel.set(forbiddenCount++ == MAX_FORBIDDEN);
                    if(cancel.get()) {
                        queue.drainTo(new ArrayList<>());
                        log("Aborting... Remote is throttling requests.");
                    }
                }
            } catch (IOException | InterruptedException ignored) {
            }
        }
        log("Consumer thread terminating");
    }
}