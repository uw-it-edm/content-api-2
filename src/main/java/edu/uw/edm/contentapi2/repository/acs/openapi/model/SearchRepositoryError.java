package edu.uw.edm.contentapi2.repository.acs.openapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

/**
 * @author Maxime Deravet Date: 11/20/18
 */
@Data
@JsonRootName("error")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRepositoryError {
    private String errorKey;
    private int statusCode;
    private String briefSummary;

    public SearchRepositoryError() {
    }
}
