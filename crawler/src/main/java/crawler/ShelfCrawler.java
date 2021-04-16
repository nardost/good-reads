package crawler;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static crawler.Configuration.shelfUrl;

@AllArgsConstructor
public class ShelfCrawler implements Runnable {

    private final String genre;
    private final BlockingQueue<Book> queue;
    private final AtomicBoolean cancelled;

    @Override
    public void run() {
        Thread.currentThread().setName(genre);
        final Connection connection = Jsoup.connect(shelfUrl + "/" + genre);
        connection.ignoreHttpErrors(true);
        final String element = "div.elementList";
        final String author = "div.elementList a.authorName";
        final String path = "div.elementList a";
        final String id = "div.elementList div.stars";
        try {
            final Document document = connection.get();
            if(Objects.nonNull(document)) {
                document.select(element).forEach(e -> {
                    final Book b = new Book();
                    final List<String> listOfGenres = new ArrayList<>();
                    listOfGenres.add(genre);
                    b.setGenres(listOfGenres);
                    b.setId(e.select(id).attr("data-resource-id"));
                    b.setPath(e.select(path).get(0).attr("href"));
                    final Author a = new Author();
                    a.setPath(e.select(author).attr("href"));
                    b.setAuthor(a);
                    try {
                        if(!cancelled.get() && !"".equals(b.getId())) {
                            queue.put(b);
                            System.out.printf("%s: Got book id %s%n", Thread.currentThread().getName(), b.getId());
                        }
                    } catch (InterruptedException ignored) {
                        System.out.printf("%s: Producer thread interrupted...%n", Thread.currentThread().getName());
                    }
                });
            }
        } catch (IOException ignored) {
        }
        System.out.printf("%s: Producer thread terminating...%n", Thread.currentThread().getId());
    }
}
