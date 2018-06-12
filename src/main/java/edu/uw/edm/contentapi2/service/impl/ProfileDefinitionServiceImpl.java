package edu.uw.edm.contentapi2.service.impl;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import edu.uw.edm.contentapi2.properties.ProfileProperties;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileDefinitionService;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

@Service
public class ProfileDefinitionServiceImpl implements ProfileDefinitionService {
    ExternalDocumentRepository<Document> documentRepository;
    ProfileProperties profileProperties;

    @Autowired
    ProfileDefinitionServiceImpl(ExternalDocumentRepository<Document> documentRepository, ProfileProperties profileProperties) {
        this.documentRepository = documentRepository;
        this.profileProperties = profileProperties;
    }

    @Override
    public ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException {
        final String contentType =  profileProperties.getContentTypeForProfile(profileId);
        final Map<String, PropertyDefinition<?>> propertyDefinition = documentRepository.getPropertyDefinition(user, contentType);
        final ProfileDefinitionV4 profileDefinitionV4 = new ProfileDefinitionV4(profileId, propertyDefinition);
        return profileDefinitionV4;
    }
}