package crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenresHarvester {

    static List<String> getAllGenres() {
        final int startPage = 1;
        final int finishPage = 14;
        final List<String> genres = new ArrayList<>();
        for(int page = startPage; page <= finishPage; page++) {
            final Connection connection = Jsoup.connect("https://www.goodreads.com/genres/list?page=" + page);
            connection.ignoreHttpErrors(true);
            final String querySelector = "div.shelfStat div a.mediumText";
            try {
                final Document document = connection.get();
                if (Objects.nonNull(document)) {
                    document.select(querySelector).forEach(e -> genres.add(e.html()));
                }
            } catch (IOException ignored) {
            }
        }
        return genres;
    }
}
