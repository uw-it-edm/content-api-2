package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.repository.acs.openapi.SearchResultTransformer;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileDefinitionService;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@Service
public class SearchResultTransformerImpl implements SearchResultTransformer {


    private ProfileDefinitionService profileDefinitionService;

    @Autowired
    public SearchResultTransformerImpl(ProfileDefinitionService profileDefinitionService) {
        this.profileDefinitionService = profileDefinitionService;
    }


    @Override
    public SearchResult toSearchResult(ResultNodeRepresentation resultNode, String profileId, User user) {
        SearchResult result = new SearchResult();

        ContentAPIDocument document = toContentApiDocument(resultNode, profileId, user);

        result.setDocument(document);
        result.setIndexName(profileId);

        result.setScore(resultNode.getSearch().getScore());

        return result;
    }

    private ContentAPIDocument toContentApiDocument(ResultNodeRepresentation resultNode, String profileId, User user) {


        ContentAPIDocument document = new ContentAPIDocument();

        document.setId(resultNode.getId());
        document.setLabel(resultNode.getProperties().getOrDefault(Constants.Alfresco.AlfrescoFields.TITLE_FQDN, null).toString());


        document.getMetadata().put("creationDate", resultNode.getCreatedAt());
        document.getMetadata().put("createdBy", resultNode.getCreatedByUser().getId());
        document.getMetadata().put("contentStreamMimeType", resultNode.getContent().getMimeType());
        document.getMetadata().put("contentStreamLength", resultNode.getContent().getSizeInBytes());
        document.getMetadata().put("lastModificationDate", resultNode.getModifiedAt());
        document.getMetadata().put("lastModificationDate", resultNode.getModifiedByUser().getId());
        document.getMetadata().put("name", resultNode.getName());


        resultNode.getProperties().forEach((key, value) -> {
            document.getMetadata().put(getContentFieldName(profileId, key, user), value);
        });

        return document;
    }

    private String getACSFieldName(String profileId, String contentFieldName, User user) {
        //TODO
        return contentFieldName;
    }

    private String getContentFieldName(String profileId, String acsFieldName, User user) {
        //TODO
        return acsFieldName;
    }


}
