package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class SearchQueryModel {

    private String query;

    private List<SearchFacet> facets = new LinkedList<>();

    private List<SearchFilter> filters = new LinkedList<>();

    private int from = 0;

    private int pageSize = 20;

    private SearchOrder searchOrder = SearchOrder.getDefaultSearchOrder();

    public void addFilter(SearchFilter filter) {
        this.filters.add(filter);
    }
}

