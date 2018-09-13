package edu.uw.edm.contentapi2.service.profile.impl;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.ExternalProfileRepository;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;

/**
 * @author Maxime Deravet Date: 6/28/18
 */
@Service
public class ProfileDefinitionServiceImpl implements ProfileDefinitionService {

    ExternalProfileRepository profileRepository;

    FieldMapper fieldMapper;

    @Autowired
    ProfileDefinitionServiceImpl(ExternalProfileRepository profileRepository, FieldMapper fieldMapper) {
        this.profileRepository = profileRepository;
        this.fieldMapper = fieldMapper;
    }

    @Override
    @Cacheable(value = "profile-definition", key = "{#profileId, #user.username}")
    public ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException {
        final String contentType = fieldMapper.getContentTypeForProfile(profileId);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = profileRepository.getPropertyDefinition(user, contentType);

        final PropertyDefinition idField = propertyDefinitions.get(RepositoryConstants.CMIS.ITEM_ID_FQDN);
        final FieldDefinition id = FieldDefinition.builder()
                .repoFieldName(idField.getId())
                .type(MappingType.fromPropertyType(idField.getPropertyType()))
                .validator("none")//TODO: implement validators
                .build();

        //TODO: should label be required on all profiles?
        final PropertyDefinition labelField = propertyDefinitions.get(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN);
        final FieldDefinition label = (labelField == null) ? null : FieldDefinition.builder()
                .repoFieldName(labelField.getId())
                .type(MappingType.fromPropertyType(labelField.getPropertyType()))
                .validator("none")//TODO: implement validators
                .build();

        final Map<String, FieldDefinition> metadata = createDocumentMetadataFields(profileId, propertyDefinitions);

        final ProfileDefinitionV4 profileDefinitionV4 = ProfileDefinitionV4.builder()
                .profile(profileId)
                .id(id)
                .label(label)
                .metadata(metadata)
                .build();


        return profileDefinitionV4;
    }

    private Map<String, FieldDefinition> createDocumentMetadataFields(String profileId, Map<String, PropertyDefinition<?>> propertyDefinitions) {
        final List<String> specialFields = Arrays.asList(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN, RepositoryConstants.CMIS.ITEM_ID_FQDN);
        final List<String> metadataKeys = propertyDefinitions.keySet()
                .stream()
                .filter(repoFieldName -> !specialFields.contains(repoFieldName)) // remove specialFields from metadata
                .collect(Collectors.toList());

        final Map<String, FieldDefinition> metadata = new HashMap<>();
        metadataKeys.forEach(metadataKey -> {

            PropertyDefinition<?> property = propertyDefinitions.get(metadataKey);
            FieldDefinition fieldDefinition = FieldDefinition.builder()
                    .repoFieldName(property.getId())
                    .type(MappingType.fromPropertyType(property.getPropertyType()))
                    .validator("none")//TODO: implement validators
                    .build();
            metadata.put(fieldMapper.convertToContentApiFieldFromRepositoryField(profileId, property.getLocalName())
                    , fieldDefinition);
        });
        return metadata;
    }



}
