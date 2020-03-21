package search;

import java.time.Duration;

public class StubSearchClient implements SearchClient {
    private final int RESPONSE_COUNT = 10;
    private SearchEngine engine;
    private long delay;

    public StubSearchClient(SearchEngine engine, long delay) {
        this.engine = engine;
        this.delay = delay;
    }

    @Override
    public SearchResponse search(String query) {
        SearchResponse response = new SearchResponse();
        for (int i = 0; i < RESPONSE_COUNT; i++) {
            response.updateWith(new SearchItem(
                    engine,
                    "Search result: " + i + " engine: " + engine + " query: " + query
                    ));
        }
        try {
            Thread.sleep(delay);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return response;
        }
    }
}
