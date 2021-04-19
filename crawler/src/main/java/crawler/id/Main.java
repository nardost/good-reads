package crawler.id;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static crawler.id.Genres.*;
import static crawler.Utils.log;
import static crawler.Utils.writeJsonFile;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final Set<String> ids = new ConcurrentSkipListSet<>();
        final IdHarvester idHarvester = new IdHarvester(GENRES_13, ids);
        idHarvester.harvest();
        log(ids.size() + " ids harvested.");
        if(!ids.isEmpty()) {
            writeJsonFile("ids", ids);
        }
    }

    /**
     * Selected genres
     */
    private static final String[] genres = new String[] {
            "art",
            "biography",
            "business",
            "chick-lit",
            "childrens",
            "christian",
            "classics",
            "comedy",
            "comics",
            "contemporary",
            "cookbooks",
            "crime",
            "fantasy",
            "fiction",
            "gay",
            "graphic-novels",
            "historical-fiction",
            "history",
            "horror",
            "humor",
            "lesbian",
            "manga",
            "memoir",
            "music",
            "mystery",
            "non-fiction",
            "paranormal",
            "philosophy",
            "poetry",
            "psychology",
            "religion",
            "romance",
            "science",
            "science-fiction",
            "self-help",
            "suspense",
            "spirituality",
            "sports",
            "thriller",
            "travel",
            "young-adult"
    };
}
