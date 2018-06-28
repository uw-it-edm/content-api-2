package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.repository.acs.openapi.SearchResultTransformer;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@Service
public class SearchResultTransformerImpl implements SearchResultTransformer {


    private ProfileFacade profileFacade;

    @Autowired
    public SearchResultTransformerImpl(ProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
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

        //TODO check if we need other fields

        document.getMetadata().put(getContentFieldName(profileId, "creationDate"), resultNode.getCreatedAt());
        document.getMetadata().put(getContentFieldName(profileId, "createdBy"), resultNode.getCreatedByUser().getId());
        document.getMetadata().put(getContentFieldName(profileId, "contentStreamMimeType"), resultNode.getContent().getMimeType());
        document.getMetadata().put(getContentFieldName(profileId, "contentStreamLength"), resultNode.getContent().getSizeInBytes());
        document.getMetadata().put(getContentFieldName(profileId, "lastModificationDate"), resultNode.getModifiedAt());
        document.getMetadata().put(getContentFieldName(profileId, "lastModificationDate"), resultNode.getModifiedByUser().getId());
        document.getMetadata().put(getContentFieldName(profileId, "name"), resultNode.getName());


        resultNode.getProperties().forEach((key, value) -> {
            document.getMetadata().put(getContentFieldName(profileId, key), value);
        });

        return document;
    }


    private String getContentFieldName(String profileId, String acsFieldName) {

        return profileFacade.convertToContentApiFieldFromFQDNRepositoryField(profileId, acsFieldName);

    }


}
