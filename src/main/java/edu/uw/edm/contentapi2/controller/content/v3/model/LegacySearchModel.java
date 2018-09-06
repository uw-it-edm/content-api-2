package edu.uw.edm.contentapi2.controller.content.v3.model;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Maxime Deravet Date: 6/20/18
 */
@Deprecated
@Data
public class LegacySearchModel {

    @NotNull
    List<String> search;
    Conjunction conjunction = Conjunction.and;


    int pageStart = 0;
    int pageSize = 20;
    String orderBy;
    SearchOrder order;

    @Deprecated
    Set<String> additionalRequestedFields;
    @Deprecated
    private boolean directDatabaseSearch = false;
}
