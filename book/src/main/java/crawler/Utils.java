package crawler;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static crawler.Parameters.BOOKS_DIR;
import static crawler.Parameters.DATA_STORE;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Utility methods like logging and file operations.
 */
public class Utils {

    /**
     * Global constants
     */
    public static final long PROGRAM_START_TIME = System.currentTimeMillis();
    public static final int THREAD_NAME_COL_WIDTH = 15;

    public static void writeJsonFile(final String fileName, Collection<?> booksInGenre) {
        final StringBuilder uniqueFileName = new StringBuilder();
        uniqueFileName.append(fileName)
                .append("_")
                .append(UUID.randomUUID()
                                .toString()
                                .toLowerCase()
                                .replace("-", ""))
                .append(".json");
        try {
            final Gson gson = new Gson();
            final Path dataDir = Path.of(DATA_STORE + File.separator + BOOKS_DIR).toAbsolutePath();
            final Path path = Paths.get(dataDir + File.separator + uniqueFileName);
            if(!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
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
