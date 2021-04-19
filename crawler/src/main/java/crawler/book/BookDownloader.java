package crawler.book;

import crawler.Book;
import lombok.AllArgsConstructor;

import java.io.IOException;
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

                final Book book = Crawler.downloadBook(id, forbiddenCount);

                if(forbiddenCount.get() > maxTolerableHttpError) {
                    log("Remote is throttling requests...");
                    break;
                }

                if(Objects.nonNull(book.getId())) {
                    books.add(book);
                    log(book.getId() + " added to collection");

                    ThumbnailGetter.downloadThumbnail(book.getThumbnail(), book.getId());

                    output.put(book.getAuthor().getPath());
                }
            } catch (InterruptedException ignored) {
                log("Interrupted....");
            } catch (IOException ioe) {
                log(ioe.getMessage());
            }
        }
        log("Terminating...");
        done.countDown();
    }
}
