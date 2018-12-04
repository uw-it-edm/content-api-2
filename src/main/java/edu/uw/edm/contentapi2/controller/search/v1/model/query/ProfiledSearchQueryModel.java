package edu.uw.edm.contentapi2.controller.search.v1.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfiledSearchQueryModel extends SearchQueryModel {
    private String profileId;

}
