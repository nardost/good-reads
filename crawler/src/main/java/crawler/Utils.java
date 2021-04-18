package crawler;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class Utils {

    private static final Gson gson = new Gson();
    private static final Path dataDir = Path.of("GOOD_READS_DATA").toAbsolutePath();
    public static final long PROGRAM_START_TIME = System.currentTimeMillis();
    public static int THREAD_NAME_COL_WIDTH = 15;

    public static void writeJsonFile(final String fileName, List<?> booksInGenre) {
        final String uniqueFileName = fileName + "_" + UUID.randomUUID().toString().toLowerCase() + ".json";
        try {
            final Path path = Paths.get(dataDir + File.separator + uniqueFileName);
            if(!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
            }
            Files.writeString(path, gson.toJson(booksInGenre), CREATE, WRITE, APPEND);
        } catch (IOException ignored) {
        }
    }

    public static void log(final String message) {

        final long elapsedTime = System.currentTimeMillis() - PROGRAM_START_TIME;

        final long millis = elapsedTime % 1000;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime - millis) % 60;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime - 1000L * seconds - millis) % 60;
        final long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime - 60000L * minutes - 1000L * seconds - millis);

        final String ms = String.format("%1$3s", millis).replace(' ', '0');
        final String ss = String.format("%1$2s", seconds).replace(' ', '0');
        final String mm = String.format("%1$2s", minutes).replace(' ', '0');
        final String hh = String.format("%1$2s", hours).replace(' ', '0');
        final String threadName = Thread.currentThread().getName();

        final String colFormat = "%1$" + THREAD_NAME_COL_WIDTH + "s";
        System.out.printf("[%s:%s:%s.%s] %s:  %s%n", hh, mm, ss, ms, String.format(colFormat, threadName), message);
    }
}
