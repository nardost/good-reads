package crawler.book;

import crawler.Author;
import crawler.Book;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static crawler.Utils.log;

public class Crawler {

    public static Book downloadBook(final String id, final AtomicInteger forbiddenCount) {
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

    public static Author downloadAuthor(final String author) {
        return null;
    }
}
