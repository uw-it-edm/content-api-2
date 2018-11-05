package edu.uw.edm.contentapi2.controller.content.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

/**
 * @author Maxime Deravet Date: 11/2/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PublicationResultResource {

    @JsonProperty("PublicationResults")
    private List<String> itemsAffected;
}
