package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchFacet {
    private String field;
    private int size = 20;
    private Order order;

}
