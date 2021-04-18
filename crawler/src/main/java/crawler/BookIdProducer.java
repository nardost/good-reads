package crawler;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static crawler.Utils.THREAD_NAME_COL_WIDTH;
import static crawler.Utils.log;

@AllArgsConstructor
public class BookIdProducer implements Runnable {

    private final String genre;
    private final BlockingQueue<Book> queue;
    private final AtomicBoolean cancelled;

    @Override
    public void run() {
        final Connection connection = Jsoup.connect("https://www.goodreads.com/shelf/show/" + genre);
        connection.ignoreHttpErrors(true);
        try {
            final Document document = connection.get();
            if(Objects.nonNull(document)) {
                document.select("div.elementList").forEach(e -> {
                    final Book b = new Book();
                    final List<String> listOfGenres = new ArrayList<>();
                    listOfGenres.add(genre);
                    b.setGenres(listOfGenres);
                    b.setId(e.select("div.elementList div.stars").attr("data-resource-id"));
                    b.setPath(e.select("div.elementList a").get(0).attr("href"));
                    final Author a = new Author();
                    a.setPath(e.select("div.elementList a.authorName").attr("href"));
                    b.setAuthor(a);
                    try {
                        if(!cancelled.get() && !"".equals(b.getId())) {
                            queue.put(b);
                            log("Got book " + b.getId());
                        }
                    } catch (InterruptedException ignored) {
                    }
                });
            }
        } catch (IOException ignored) {
        }
        log("Producer thread terminating...");
    }
}
