package edu.uw.edm.contentapi2.controller.search.v1.model.result;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResultContainer {
    List<SearchResult> searchResults = new LinkedList<>();

    private List<FacetResult> facets = new LinkedList<>();
    private long totalCount;
    private String timeTaken;

    public SearchResultContainer() {
    }

}
