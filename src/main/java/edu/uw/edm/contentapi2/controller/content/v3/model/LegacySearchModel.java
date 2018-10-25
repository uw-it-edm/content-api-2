package edu.uw.edm.contentapi2.controller.content.v3.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Maxime Deravet Date: 6/20/18
 */
@Deprecated
@Data
public class LegacySearchModel {

    @NotNull
    private List<String> search;
    private Conjunction conjunction = Conjunction.and;

    private int pageStart = 0;
    private int pageSize = 20;
    private String orderBy;
    private SearchOrder order;

}
