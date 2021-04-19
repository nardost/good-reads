package crawler.book;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static crawler.book.Parameters.DATA_STORE;

public class ThumbnailGetter {
    public static void downloadThumbnail(final String thumbnail, final String id) throws IOException {
        final URL url = new URL(thumbnail);
        final Path thumbnailsDir = Path.of(DATA_STORE + File.separator + "thumbnails").toAbsolutePath();
        if(!Files.exists(thumbnailsDir)) {
            Files.createDirectory(thumbnailsDir);
        }
        final Path thumbnailPath = Path.of(thumbnailsDir + File.separator + id + ".jpg").toAbsolutePath();
        final ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        final FileOutputStream fileOutputStream = new FileOutputStream(thumbnailPath.toFile());
        final FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
}
