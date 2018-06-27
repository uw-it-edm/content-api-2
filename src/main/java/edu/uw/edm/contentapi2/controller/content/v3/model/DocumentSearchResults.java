package edu.uw.edm.contentapi2.controller.content.v3.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;

/**
 * @author Maxime Deravet Date: 6/20/18
 */

@Builder
public class DocumentSearchResults {


    @Builder.Default
    private Set<ContentAPIDocument> documents = new HashSet<>();

    private int totalCount;

    public int getResultListSize() {
        return documents.size();
    }

}
