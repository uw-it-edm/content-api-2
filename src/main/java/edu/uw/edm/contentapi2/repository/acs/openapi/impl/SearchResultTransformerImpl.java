package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.repository.acs.openapi.SearchResultTransformer;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.CONTENT_STREAM_LENGTH_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.CONTENT_STREAM_MIME_TYPE_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.CREATED_BY_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.CREATION_DATE_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.LAST_MODIFICATION_DATE_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.LAST_MODIFIER_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.NAME_FQDN;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;

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
    public SearchResult toSearchResult(ResultNodeRepresentation resultNode, String profileId, User user) throws NoSuchProfileException {
        final ContentAPIDocument document = toContentApiDocument(resultNode, profileId, user);

        final SearchResult result = new SearchResult();
        result.setDocument(document);
        result.setIndexName(profileId);
        result.setScore(resultNode.getSearch().getScore());

        return result;
    }

    private ContentAPIDocument toContentApiDocument(ResultNodeRepresentation resultNode, String profileId, User user) throws NoSuchProfileException {
        final ContentAPIDocument document = new ContentAPIDocument();

        document.setId(resultNode.getId());
        document.setLabel((String) resultNode.getProperties().get(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN));
        document.getMetadata().put(PROFILE_ID, profileId);
        //TODO check if we need other fields

        document.getMetadata().putAll(convertField(user, profileId, CREATION_DATE_FQDN, resultNode.getCreatedAt()));
        if (resultNode.getCreatedByUser() != null) {
            document.getMetadata().putAll(convertField(user, profileId, CREATED_BY_FQDN, resultNode.getCreatedByUser().getId()));
        }
        if (resultNode.getContent() != null) {
            document.getMetadata().putAll(convertField(user, profileId, CONTENT_STREAM_MIME_TYPE_FQDN, resultNode.getContent().getMimeType()));
            document.getMetadata().putAll(convertField(user, profileId, CONTENT_STREAM_LENGTH_FQDN, resultNode.getContent().getSizeInBytes()));
        }

        document.getMetadata().putAll(convertField(user, profileId, LAST_MODIFICATION_DATE_FQDN, resultNode.getModifiedAt()));

        if (resultNode.getModifiedByUser() != null) {
            document.getMetadata().putAll(convertField(user, profileId, LAST_MODIFIER_FQDN, resultNode.getModifiedByUser().getId()));
        }

        document.getMetadata().putAll(convertField(user, profileId, NAME_FQDN, resultNode.getName()));

        final Map<String, Object> contentApiMetadata = profileFacade.convertMetadataToContentApiDataTypes(resultNode, user, profileId);
        document.getMetadata().putAll(contentApiMetadata);


        return document;
    }

    private final Map<String, Object> convertField(User user, String profileId, String fqdnRepoFieldName, Object fieldValue) throws NoSuchProfileException {
        return profileFacade.convertMetadataFieldToContentApiDataType(user, profileId, fqdnRepoFieldName, fieldValue);

    }

}
