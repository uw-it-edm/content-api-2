package edu.uw.edm.contentapi2.service;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.apache.chemistry.opencmis.client.api.Document;

import java.util.Map;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.exceptions.UndefinedFieldException;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

public interface ProfileFacade {

    String getRepoFQDNFieldName(String contentFieldName, String profileId, User user) throws NoSuchProfileException;

    ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException;

    String getContentTypeForProfile(String profileId) throws NoSuchProfileException;

    String convertToContentApiFieldFromRepositoryField(String profile, String repoFieldLocalName);

    String convertToContentApiFieldFromFQDNRepositoryField(String profile, String fqdnRepoFieldName);

    Map<String, Object> convertMetadataToContentApiDataTypes(ResultNodeRepresentation resultNode, User user, String profile) throws NoSuchProfileException;

    Map<String, Object> convertMetadataToContentApiDataTypes(Document cmisDocument, User user, String profile) throws NoSuchProfileException;

    Map<String, Object> convertMetadataFieldToContentApiDataType(User user, String profile, String fqdnRepoFieldName, Object fieldValue) throws NoSuchProfileException;

    Object convertToContentApiDataType(String profileId, User user, String repoFieldLocalName, Object value) throws NoSuchProfileException, UndefinedFieldException;

    Object convertToRepoDataType(String profileId, User user, String fqdnRepoFieldName, Object value) throws NoSuchProfileException;
}
