package crawler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static crawler.Utils.log;
import static crawler.Parameters.POISON_PILL;
import static crawler.Parameters.streamLimit;
import static crawler.Parameters.numberOfWorkerThreads;

/**
 * A Runnable that pumps book ids into the pipeline.
 * Producer
 */
public class BookIdSource implements Runnable {

    private final BufferedReader sourceReader;
    private final BlockingQueue<String> queue;
    private final Harvest harvest;

    private final AtomicBoolean cancel;

    public BookIdSource(final String sourceFile, final BlockingQueue<String> queue, Harvest harvest) {
        try {
            this.sourceReader = Files.newBufferedReader(Paths.get(sourceFile));
            this.queue = queue;
            this.cancel = new AtomicBoolean(false);
            this.harvest = harvest;
        } catch (IOException ioe) {
            throw new RuntimeException("I/O exception while reading " + sourceFile);
        }
    }

    @Override
    public void run() {
        final Gson gson = new Gson();
        final Set<String> ids = gson.fromJson(sourceReader, new TypeToken<Set<String>>() {}.getType());
        ids.stream().limit(streamLimit).forEach(id -> {
            if(cancel.get()) {
                return;
            }
            try {
                if(!harvest.containsBook(id)) {
                    queue.put(id);
                }
            } catch (InterruptedException ignored) {
            }
        });

        // One poison pill for each downloader
        IntStream.range(0, numberOfWorkerThreads).forEach(i -> {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException ignored) {
            }
        });
        log("Terminating...");
    }

    public void cancel() {
        this.cancel.set(true);
        queue.clear();
    }
}
