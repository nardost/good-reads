package crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GoodReadsScrapper {

    private static final int LAST_PAGE = 14;

    private List<String> getGenres(final int lastPage) throws IOException {
        final List<String> genres = new ArrayList<>();
        final String rootUrl = "https://www.goodreads.com/genres/list";
        for(int page = 1; page <= lastPage; page++) {
            final Connection connection = Jsoup.connect(rootUrl + "?page=" + page);
            connection.ignoreHttpErrors(true);
            final int statusCode = connection.execute().statusCode();
            if(statusCode != 200) throw new RuntimeException("HTTP ERROR");
            final String querySelector = "div.shelfStat div a.mediumText";
            final Document document = connection.get();
            if (Objects.nonNull(document)) {
                document.select(querySelector).forEach(e -> genres.add(e.html()));
            }
        }
        return genres;
    }

    private List<String[]> getTitles(final Stream<String> genres) {
        final List<String[]> books = new ArrayList<>();
        final String rootUrl = "https://www.goodreads.com/shelf/show";
        genres.forEach(genre -> {
            final Connection connection = Jsoup.connect(rootUrl + "/" + genre);
            connection.ignoreHttpErrors(true);
            final String elementSelector = "div.elementList";
            final String titleSelector = elementSelector + " a.bookTitle";
            final String authorSelector = elementSelector + " a.authorName span";
            final String linkSelector = elementSelector + " a";
            final String imgSelector = elementSelector + " img";
            try {
                final Document document = connection.get();
                if(Objects.nonNull(document)) {
                    document.select(elementSelector).forEach(e -> {
                        final String[] bookInfo = new String[4];
                        bookInfo[0] = e.select(titleSelector).html();
                        bookInfo[1] = e.select(authorSelector).html();
                        bookInfo[2] = e.select(imgSelector).attr("src");
                        bookInfo[3] = e.select(linkSelector).get(0).attr("href");
                        books.add(bookInfo);
                    });
                }
            } catch (IOException ignored) {
            }
        });
        return books;
    }

    public static void main(String[] args) throws IOException {
        GoodReadsScrapper scrapper = new GoodReadsScrapper();
        List<String[]> books = scrapper.getTitles(Stream.of("writing", "yeti"));
        books.forEach(b -> System.out.printf("%s,%s,%s,%s%n", b[0], b[1], b[2], b[3]));
    }
}
