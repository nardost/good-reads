package crawler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static crawler.Utils.log;
import static crawler.Parameters.POISON_PILL;
import static crawler.Parameters.idStreamLimit;
import static crawler.Parameters.numberOfDownloaders;

/**
 * A Runnable that pumps book ids into the pipeline.
 * Producer
 */
public class BookIdSource implements Runnable {

    private final BufferedReader sourceReader;
    private final BlockingQueue<String> queue;

    private final AtomicBoolean cancel;

    public BookIdSource(final String sourceFile, final BlockingQueue<String> queue) {
        try {
            this.sourceReader = Files.newBufferedReader(Paths.get(sourceFile));
            this.queue = queue;
            this.cancel = new AtomicBoolean(false);
        } catch (IOException ioe) {
            throw new RuntimeException("I/O exception while reading " + sourceFile);
        }
    }

    @Override
    public void run() {
        final Gson gson = new Gson();
        final Set<String> ids = gson.fromJson(sourceReader, new TypeToken<Set<String>>() {}.getType());
        ids.stream().limit(idStreamLimit).forEach(id -> {
            if(cancel.get()) {
                return;
            }
            try {
                if(!Harvest.getHarvestedBooks().contains(id)) {
                    queue.put(id);
                }
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

    public void cancel() {
        this.cancel.set(true);
        queue.drainTo(new ArrayList<>());
    }
}
