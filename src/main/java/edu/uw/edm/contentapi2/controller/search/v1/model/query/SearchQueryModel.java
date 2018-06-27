package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import java.util.LinkedList;
import java.util.List;

public class SearchQueryModel {

    private String query;

    private List<SearchFacet> facets = new LinkedList<>();

    private List<SearchFilter> filters = new LinkedList<>();

    private int from = 0;

    private int pageSize = 20;

    private SearchOrder searchOrder = SearchOrder.getDefaultSearchOrder();


    public SearchQueryModel() {
    }


    public SearchOrder getSearchOrder() {
        return searchOrder;
    }

    public void setSearchOrder(SearchOrder searchOrder) {
        this.searchOrder = searchOrder;
    }

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<SearchFacet> getFacets() {
        return facets;
    }

    public void setFacets(List<SearchFacet> facets) {
        this.facets = facets;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
