package crawler;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static crawler.Configuration.bookUrl;

@AllArgsConstructor
public class BookCrawler implements Runnable {

    private final Map<String, List<Book>> books;
    private final BlockingQueue<Book> queue;
    private final AtomicBoolean cancel;

    @Override
    public void run() {
        int forbiddenCount = 0;
        while(!cancel.get()) {
            try {
                final Book book = queue.take();
                System.out.printf("%s: Trying to get details for book %s%n", Thread.currentThread().getName(), book.getId());
                final Connection connection = Jsoup.connect(bookUrl + "/" + book.getId());
                connection.ignoreHttpErrors(true);
                if (connection.execute().statusCode() == 200) {
                    final String element = "div#topcol";
                    final String title = "div#topcol h1#bookTitle";
                    final String thumbnail = "div#topcol div#imagecol div.bookCoverPrimary img#coverImage";
                    final String blurb = "div#topcol div#description span#freeText";
                    final String blurbId = "div#topcol div#description a";
                    final Document document = connection.get();
                    if (Objects.nonNull(document)) {
                        document.select(element).forEach(e -> {
                            book.setTitle(e.select(title).text());
                            book.setThumbnail(e.select(thumbnail).attr("src"));
                            final String dataTextId = e.select(blurbId).attr("data-text-id");
                            book.setBlurb(e.select(blurb + dataTextId).text());
                        });
                        final String key = book.getGenres().isEmpty() ? "unknown-genre" : book.getGenres().get(0);
                        if(Objects.nonNull(books.get(key))) {
                            books.get(key).add(book);
                        } else {
                            final List<Book> booksInGenre = new ArrayList<>();
                            booksInGenre.add(book);
                            books.put(key, booksInGenre);
                        }
                        System.out.printf("%s: Got details for %s%n", Thread.currentThread().getName(), book.getId());
                    }
                } else {
                    cancel.set(forbiddenCount++ == 5);
                    if(cancel.get()) {
                        queue.drainTo(new ArrayList<>());
                        System.out.printf("%s: Aborting... Program is throttled.%n", Thread.currentThread().getName());
                    }
                }
            } catch (IOException | InterruptedException ignored) {
            }
        }
        System.out.printf("%s: Consumer thread terminating...%n", Thread.currentThread().getName());
    }
}
