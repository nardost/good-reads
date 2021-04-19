package crawler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Book implements Comparable<Book> {
    private String id;
    private String title;
    private String path;
    private Set<String> genres;
    private String thumbnail;
    private String blurb;
    private int pages;
    private Author author;

    @Override
    public int compareTo(Book o) {
        return Integer.compare(Integer.parseInt(this.id), Integer.parseInt(o.getId()));
    }
}
