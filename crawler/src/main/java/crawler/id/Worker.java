package crawler.id;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static crawler.id.Harvested1.alreadyHarvestedIds1;
import static crawler.id.Harvested2.alreadyHarvestedIds2;
import static crawler.id.Harvested3.alreadyHarvestedIds3;
import static crawler.id.Harvested4.alreadyHarvestedIds4;
import static crawler.id.Harvested5.alreadyHarvestedIds5;
import static crawler.id.Harvested6.alreadyHarvestedIds6;
import static crawler.Utils.log;

@AllArgsConstructor
public class Worker implements Runnable {

    private final Stream<String> genres;
    private final Set<String> ids;
    private final CountDownLatch done;

    @Override
    public void run() {
        AtomicInteger count = new AtomicInteger();
        genres.forEach(shelf -> {
            final String shelfUrl = "https://www.goodreads.com/shelf/show/" + shelf;
            final Connection connection = Jsoup.connect(shelfUrl);
            connection.ignoreHttpErrors(true);
            try {
                int statusCode;
                if ((statusCode = connection.execute().statusCode()) != 200) {
                    throw new HttpStatusException("HTTP Error", statusCode, shelfUrl);
                }
                final Document document = connection.get();
                if(Objects.nonNull(document)) {
                    document.select("div.elementList").forEach(e -> {
                        final String id = e.select("div.elementList div.stars").attr("data-resource-id");
                        if(id.matches("\\d+") && isNotHarvestedYet(id)) {
                            ids.add(id);
                            count.getAndIncrement();
                        }
                    });
                }
            } catch (IOException ignored) {
            }
        });
        log(count.get() + " ids harvested");
        done.countDown();
    }

    private boolean isNotHarvestedYet(final String id) {
        return !alreadyHarvestedIds1.containsKey(id) &&
                !alreadyHarvestedIds2.containsKey(id) &&
                !alreadyHarvestedIds3.containsKey(id) &&
                !alreadyHarvestedIds4.containsKey(id) &&
                !alreadyHarvestedIds5.containsKey(id) &&
                !alreadyHarvestedIds6.containsKey(id);
    }
}
