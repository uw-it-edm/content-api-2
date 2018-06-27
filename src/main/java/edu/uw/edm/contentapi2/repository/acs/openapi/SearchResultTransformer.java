package edu.uw.edm.contentapi2.repository.acs.openapi;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
public interface SearchResultTransformer {
    SearchResult toSearchResult(ResultNodeRepresentation resultNode, String profileId, User user);
}
