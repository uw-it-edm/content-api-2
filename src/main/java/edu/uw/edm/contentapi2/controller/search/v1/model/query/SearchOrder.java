package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import lombok.Data;

@Data
public class SearchOrder {

    public static final String SCORE_ORDER = "_score";
    private String term;
    private Object missing;
    private String script = null;

    private Order order;

    public SearchOrder() {

    }

    public SearchOrder(String term, String order) {
        this.term = term;
        this.order = Order.valueOf(order);
    }

    public SearchOrder(String term, Order order) {
        this.term = term;
        this.order = order;
    }

    public static SearchOrder getDefaultSearchOrder() {
        return new SearchOrder(SCORE_ORDER, Order.desc);
    }

}
