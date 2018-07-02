package edu.uw.edm.contentapi2.service;

import org.springframework.cache.annotation.Cacheable;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

public interface ProfileFacade {

    String getRepoFQDNFieldName(String contentFieldName, String profileId, User user) throws NoSuchProfileException;

    ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException;

    String getContentTypeForProfile(String profileId) throws NoSuchProfileException;

    String convertToContentApiFieldFromRepositoryField(String profile, String repoFieldLocalName);

    String convertToContentApiFieldFromFQDNRepositoryField(String profile, String repoFieldLocalName);

}
