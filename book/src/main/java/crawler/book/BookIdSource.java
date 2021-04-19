package crawler.book;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

import static crawler.Utils.log;
import static crawler.Parameters.POISON_PILL;
import static crawler.Parameters.idStreamLimit;
import static crawler.Parameters.numberOfDownloaders;

public class BookIdSource implements Runnable {

    private final BufferedReader reader;
    private final BlockingQueue<String> queue;

    public BookIdSource(final String sourceFile, final BlockingQueue<String> queue) {
        try {
            this.reader = Files.newBufferedReader(Paths.get(sourceFile));
            this.queue = queue;
        } catch (IOException ioe) {
            throw new RuntimeException("I/O exception while reading " + sourceFile);
        }
    }

    @Override
    public void run() {
        final Gson gson = new Gson();
        final Set<String> ids = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
        ids.stream().limit(idStreamLimit).forEach(id -> {
            try {
                queue.put(id);
                log("Enqueued book id " + id);
            } catch (InterruptedException ignored) {
            }
        });
        /*
         * One poison pill for each downloader
         */
        IntStream.range(0, numberOfDownloaders).forEach(i -> {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException ignored) {
            }
        });
        log("Terminating...");
    }
}
