package search;

public class SearchItem {
    public SearchEngine engine;
    public String text;

    public SearchItem(SearchEngine engine, String text) {
        this.engine = engine;
        this.text = text;
    }
}
