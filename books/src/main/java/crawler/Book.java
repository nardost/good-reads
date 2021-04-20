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
    private Set<Author> authors;
    private Set<String> genres;
    private String blurb;
    private int pages;
    private String thumbnail;

    @Override
    public int compareTo(Book o) {
        return Integer.compare(Integer.parseInt(this.id), Integer.parseInt(o.getId()));
    }
}
