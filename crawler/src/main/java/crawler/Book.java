package crawler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Book {
    private String id;
    private String title;
    private String path;
    private Set<String> genres;
    private String thumbnail;
    private String blurb;
    private int pages;
    private Author author;
}
