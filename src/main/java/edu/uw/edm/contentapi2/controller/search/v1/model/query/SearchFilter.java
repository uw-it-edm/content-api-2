package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SimpleSearchFilter.class)
public interface SearchFilter {
}
