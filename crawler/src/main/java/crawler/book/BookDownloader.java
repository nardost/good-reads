package crawler.book;

import crawler.Author;
import crawler.Book;
import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static crawler.Utils.log;
import static crawler.book.Parameters.POISON_PILL;
import static crawler.book.Parameters.maxTolerableHttpError;

@AllArgsConstructor
public class BookDownloader implements Runnable {

    private final BlockingQueue<String> input;
    private final BlockingQueue<String> output;
    private final Set<Book> books;
    private final CountDownLatch done;

    @Override
    public void run() {

        final AtomicInteger forbiddenCount = new AtomicInteger(0);

        while(true) {
            try {
                final String id = input.take();

                if(POISON_PILL.equals(id)) {
                    log("Got the poison pill");
                    break;
                }

                final Book book = downloadBook(id, forbiddenCount);

                if(forbiddenCount.get() > maxTolerableHttpError) {
                    log("Remote is throttling requests...");
                    break;
                }

                if(Objects.nonNull(book.getId())) {
                    books.add(book);
                    log(book.getId() + " added to collection");

                    downloadThumbnail(book.getThumbnail());

                    output.put(book.getAuthor().getPath());
                }
            } catch (InterruptedException ignored) {
                log("Interrupted....");
            }
        }
        log("Terminating...");
        done.countDown();
    }

    private Book downloadBook(final String id, final AtomicInteger forbiddenCount) {
        final Book book = new Book();
        final String bookUrl = "https://www.goodreads.com/book/show/" + id;
        final Connection connection = Jsoup.connect(bookUrl);
        connection.ignoreHttpErrors(true);
        try {
            int statusCode;
            if ((statusCode = connection.execute().statusCode()) != 200) {
                forbiddenCount.getAndIncrement();
                throw new HttpStatusException("HTTP error: " + statusCode, statusCode, bookUrl);
            }
            final Document document = connection.get();
            if(Objects.nonNull(document)) {
                final Element l = document.selectFirst("div.leftContainer div#topcol");
                book.setId(id);
                book.setTitle(l.selectFirst("h1#bookTitle").text());
                book.setThumbnail(l.selectFirst("div#imagecol div.bookCoverPrimary img#coverImage").attr("src"));

                final String dataTextId = l.select("div#description a").attr("data-text-id");
                book.setBlurb(l.select("span#freeText" + dataTextId).text());

                final String pages = l.selectFirst("div#details div.row").getElementsByAttributeValue("itemprop", "numberOfPages").text();
                book.setPages(Integer.parseInt(pages.replaceAll("[\\sa-zA-Z]", "")));

                final Author author = new Author();
                author.setPath(l.selectFirst("div#bookAuthors").selectFirst("a.authorName").attr("href"));
                book.setAuthor(author);

                final Set<String> genres = new HashSet<>();
                final Element r = document.selectFirst("div.rightContainer");
                if("Genres".equals(r.select("div.stacked h2 a").text())) {
                    r.select("div.elementList")
                            .forEach(e -> e.select("div.left a")
                                    .forEach(a -> genres.add(a.text())));
                    book.setGenres(genres);
                }
            }
        } catch (IOException | RuntimeException e) {
            /*
             * If any of the fields are not found, reject the book.
             */
            book.setId(null);
            log(e.getMessage());
        }
        return book;
    }

    private void downloadThumbnail(final String thumbnail) {
    }
}
