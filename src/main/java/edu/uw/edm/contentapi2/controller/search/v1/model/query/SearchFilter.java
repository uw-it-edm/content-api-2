package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import lombok.Data;

@Data
public class SearchFilter {
    private String field;

    private String term;

    private boolean not = false;

    public SearchFilter(String field, String term, boolean not) {
        this.field = field;
        this.term = term;
        this.not = not;
    }
}
