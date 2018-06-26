package edu.uw.edm.contentapi2.service.impl;

import org.apache.chemistry.opencmis.client.api.Document;
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
import edu.uw.edm.contentapi2.common.impl.YamlFieldMapper;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileDefinitionService;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

@Service
public class ProfileDefinitionServiceImpl implements ProfileDefinitionService {
    ExternalDocumentRepository<Document> documentRepository;
    YamlFieldMapper yamlFieldMapper;
    FieldMapper fieldMapper;

    @Autowired
    ProfileDefinitionServiceImpl(ExternalDocumentRepository<Document> documentRepository, YamlFieldMapper yamlFieldMapper, FieldMapper fieldMapper) {
        this.documentRepository = documentRepository;
        this.yamlFieldMapper = yamlFieldMapper;
        this.fieldMapper = fieldMapper;
    }


    @Cacheable(value = "profile-definition", key = "{#profileId, #user.username}")
    @Override
    public ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException {
        final String contentType = yamlFieldMapper.getContentTypeForProfile(profileId);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = documentRepository.getPropertyDefinition(user, contentType);

        final PropertyDefinition idField = propertyDefinitions.get(Constants.Alfresco.AlfrescoFields.ITEM_ID_FQDN);
        final FieldDefinition id = FieldDefinition.builder()
                .repoFieldName(idField.getId())
                .type(MappingType.fromPropertyType(idField.getPropertyType()))
                .validator("none")//TODO: implement validators
                .build();

        //TODO: should label be required on all profiles?
        final PropertyDefinition labelField = propertyDefinitions.get(Constants.Alfresco.AlfrescoFields.LABEL_FQDN);
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
        final List<String> specialFields = Arrays.asList(Constants.Alfresco.AlfrescoFields.LABEL_FQDN, Constants.Alfresco.AlfrescoFields.ITEM_ID_FQDN);
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