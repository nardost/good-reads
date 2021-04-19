package crawler.id;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor
public class IdHarvester {

    private final String[] genres;
    private final Set<String> ids;

    public void harvest() throws InterruptedException {

        final int n = genres.length;
        final int nThreads = 10;
        final CountDownLatch done = new CountDownLatch(nThreads);

        Stream<Worker> workers = IntStream.range(1, 1 + nThreads).mapToObj(i -> new Worker(Arrays.stream(genres, (i - 1) * n / nThreads, i * n / nThreads), ids, done));

        final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        workers.forEach(executor::execute);
        executor.shutdown();
        if(!executor.awaitTermination(30L, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        done.await();
    }
}
