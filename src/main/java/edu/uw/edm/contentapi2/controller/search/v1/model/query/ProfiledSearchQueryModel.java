package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import lombok.Data;

@Data
public class ProfiledSearchQueryModel extends SearchQueryModel {
    private String profileId;

}
