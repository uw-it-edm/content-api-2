package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import java.util.ArrayList;
import java.util.List;

import edu.uw.edm.contentapi2.controller.content.v3.model.Conjunction;
import lombok.Data;

@Data
public class ComplexSearchFilter implements SearchFilter {
    private final List<SearchFilter> filters = new ArrayList<>();
    private Conjunction conjunction = Conjunction.and;
    private boolean not = false;

    public ComplexSearchFilter() {
    }

    public void addFilter(SearchFilter filter) {
        this.filters.add(filter);
    }
}
