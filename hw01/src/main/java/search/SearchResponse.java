package search;

import java.util.ArrayList;

public class SearchResponse {
    private ArrayList<SearchItem> responses;

    public SearchResponse() {
        this.responses = new ArrayList<SearchItem>();
    }

    public void updateWith(SearchItem response) {
        responses.add(response);
    }

    public void updateWith(SearchResponse response) {
        responses.addAll(response.getResponses());
    }

    public ArrayList<SearchItem> getResponses() {
        return responses;
    }
}
