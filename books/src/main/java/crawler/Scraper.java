package crawler;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static crawler.Parameters.DATA_STORE;
import static crawler.Parameters.THUMBNAILS_DIR;
import static crawler.Utils.log;

public class Scraper {

    /**
     * Scarp book detail
     * @param id book id
     * @param forbiddenCount atomic-int to used to abort when remote throttles
     * @return Book with detailed info
     */
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

                book.setId(id);

                final Element l = document.selectFirst("div.leftContainer div#topcol");

                book.setTitle(l.selectFirst("h1#bookTitle").text());

                book.setThumbnail(l.selectFirst("div#imagecol div.bookCoverPrimary img#coverImage").attr("src"));

                final String dataTextId = l.select("div#description a").attr("data-text-id");
                book.setBlurb(l.select("span#freeText" + dataTextId).text());

                final String pages = l.selectFirst("div#details div.row").getElementsByAttributeValue("itemprop", "numberOfPages").text();
                book.setPages(Integer.parseInt(pages.replaceAll("[\\sa-zA-Z]", "")));

                final String published = l.select("div#details div.row")
                        .stream()
                        .filter(e -> !e.getElementsContainingText("Published").isEmpty())
                        .findFirst()
                        .map(Element::text)
                        .orElse("");
                book.setPublished(published.replace("Published ", "").split(" by ")[0]);

                final Set<Author> authors = l.selectFirst("div#bookAuthors").select("a.authorName")
                        .stream()
                        .map(a -> {
                            final Author author = new Author();
                            final String authorName = a.select("span").text();
                            final String authorPath = a.attr("href");
                            author.setName(authorName);
                            author.setPath(authorPath);
                            author.setId(authorPath.substring(authorPath.lastIndexOf('/') + 1).split("\\.")[0]);
                            return author;
                        }).collect(Collectors.toSet());
                book.setAuthors(authors);

                final Element r = document.selectFirst("div.rightContainer");
                if("Genres".equals(r.select("div.stacked h2 a").text())) {
                    final Set<String> genres = new HashSet<>();
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
        }
        return book;
    }

    /**
     * Download thumbnail
     * @param thumbnail URL of thumbnail
     * @param id book id
     * @throws IOException
     */
    public static void downloadThumbnail(final String thumbnail, final String id) throws IOException {
        final URL url = new URL(thumbnail);
        final String extension = "." + thumbnail.substring(thumbnail.lastIndexOf('.') + 1);
        final Path thumbnailsDir = Path.of(DATA_STORE + File.separator + THUMBNAILS_DIR).toAbsolutePath();
        if(!Files.exists(thumbnailsDir)) {
            Files.createDirectories(thumbnailsDir);
        }
        final Path thumbnailPath = Path.of(thumbnailsDir + File.separator + id + extension).toAbsolutePath();
        final ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        final FileOutputStream fileOutputStream = new FileOutputStream(thumbnailPath.toFile());
        final FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
}
