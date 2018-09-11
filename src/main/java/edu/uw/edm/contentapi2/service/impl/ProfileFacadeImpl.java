package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.exceptions.UndefinedFieldException;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.FieldConversionService;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;
import edu.uw.edm.contentapi2.service.util.DataTypeUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.ALFRESCO_SYSTEM_PREFIX;

@Slf4j
@Service
public class ProfileFacadeImpl implements ProfileFacade {

    private ProfileDefinitionService profileDefinitionService;
    private FieldConversionService fieldConversionService;
    private FieldMapper fieldMapper;

    @Autowired
    public ProfileFacadeImpl(ProfileDefinitionService profileDefinitionService, FieldConversionService fieldConversionService, FieldMapper fieldMapper) {
        this.profileDefinitionService = profileDefinitionService;
        this.fieldConversionService = fieldConversionService;
        this.fieldMapper = fieldMapper;
    }

    @Override
    public String getRepoFQDNFieldName(String contentFieldName, String profileId, User user) throws NoSuchProfileException {
        return fieldConversionService.getRepoFQDNFieldName(contentFieldName, profileId, user);
    }

    @Override
    public ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException {
        return profileDefinitionService.getProfileDefinition(profileId, user);
    }

    @Override
    public String getContentTypeForProfile(String profileId) throws NoSuchProfileException {
        return fieldMapper.getContentTypeForProfile(profileId);
    }

    @Override
    public String convertToContentApiFieldFromRepositoryField(String profile, String repoFieldLocalName) {
        return fieldMapper.convertToContentApiFieldFromRepositoryField(profile, repoFieldLocalName);
    }

    @Override
    public String convertToContentApiFieldFromFQDNRepositoryField(String profile, String fqdnRepoFieldName) {
        String localName = Iterables.getLast(Splitter.on(':').split(fqdnRepoFieldName));

        return fieldMapper.convertToContentApiFieldFromRepositoryField(profile, localName);
    }


    @Override
    public Map<String, Object> convertMetadataToContentApiDataTypes(ResultNodeRepresentation resultNode, User user, String profile) throws NoSuchProfileException {
        final Map<String, Object> convertedMetadata = new HashMap<>();
        for (Map.Entry<String, Object> property : resultNode.getProperties().entrySet()) {
            final Map<String, Object> convertedMetadataField = convertMetadataFieldToContentApiDataType(user, profile, property.getKey(), property.getValue());
            convertedMetadata.putAll(convertedMetadataField);
        }
        return convertedMetadata;
    }

    @Override
    public Map<String, Object> convertMetadataToContentApiDataTypes(Document cmisDocument, User user, String profile) throws NoSuchProfileException {
        final Map<String, Object> convertedMetadata = new HashMap<>();
        for (Property property : cmisDocument.getProperties()) {
            final Map<String, Object> convertedMetadataField = convertMetadataFieldToContentApiDataType(user, profile, property.getLocalName(), property.getValue());
            convertedMetadata.putAll(convertedMetadataField);
        }
        return convertedMetadata;
    }

    @Override
    public Map<String, Object> convertMetadataFieldToContentApiDataType(User user, String profile, String fqdnRepoFieldName, Object fieldValue) throws NoSuchProfileException {
        Map<String, Object> convertedMetadataField = new HashMap<>();

        if (!fqdnRepoFieldName.startsWith(ALFRESCO_SYSTEM_PREFIX)) { // do not share system properties
            final String fieldName = this.convertToContentApiFieldFromRepositoryField(profile, fqdnRepoFieldName);
            try {
                final Object convertedFieldValue = this.convertToContentApiDataType(profile, user, fqdnRepoFieldName, fieldValue);
                convertedMetadataField.put(fieldName, convertedFieldValue);
            } catch (UndefinedFieldException undefinedFieldException) {
                log.trace(undefinedFieldException.getMessage());
            }

        }
        return convertedMetadataField;
    }


    @Override  //TODO: convert this to private and update unit tests
    public Object convertToContentApiDataType(String profileId, User user, String fqdnRepoFieldName, Object value) throws NoSuchProfileException, UndefinedFieldException {
        final ProfileDefinitionV4 profileDefinition = getProfileDefinition(profileId, user);
        final String contentApiFieldName = convertToContentApiFieldFromFQDNRepositoryField(profileId, fqdnRepoFieldName);

        final FieldDefinition fieldDefinition = getFieldDefinition(profileDefinition, fqdnRepoFieldName, contentApiFieldName);
        if (fieldDefinition == null) {
            Metrics.counter("edm.repo.field.undefined", Tags.of(Tag.of("profile", profileId), Tag.of("field", fqdnRepoFieldName))).increment();
            throw new UndefinedFieldException("Unable to determine FieldDefinition for '" + fqdnRepoFieldName + "' in profile '" + profileId + "'");
        }
        switch (fieldDefinition.getType()) {
            case date:
                value = DataTypeUtils.convertToTimeStamp(value);
                break;
            case bool:
                value = DataTypeUtils.convertToBoolean(value);
                break;
            case integer:
                value = DataTypeUtils.convertToInteger(value);
                break;
            default:
                break;
        }
        return value;

    }

    @Override
    public Object convertToRepoDataType(String profileId, User user, String fqdnRepoFieldName, Object value) throws NoSuchProfileException {
        final String contentApiFieldName = this.convertToContentApiFieldFromFQDNRepositoryField(profileId, fqdnRepoFieldName);
        final ProfileDefinitionV4 profileDefinition = getProfileDefinition(profileId, user);

        final FieldDefinition fieldDefinition = getFieldDefinition(profileDefinition, fqdnRepoFieldName, contentApiFieldName);
        checkNotNull(fieldDefinition, "Unable to determine FieldDefinition for '%s' in profile '%s'", contentApiFieldName, profileId);

        final Object convertedValue;
        switch (fieldDefinition.getType()) {
            case date:
                convertedValue = DataTypeUtils.convertToLosAngelesDate(value);
                break;
            case bool:
                convertedValue = DataTypeUtils.convertToBoolean(value);
                break;
            case integer:
                convertedValue = DataTypeUtils.convertToInteger(value);
                break;
            default:
                convertedValue = value;
                break;
        }
        return convertedValue;

    }

    private FieldDefinition getFieldDefinition(ProfileDefinitionV4 profileDefinition, String
            fqdnRepoFieldName, String contentApiFieldName) {
        FieldDefinition fieldDefinition;
        if (RepositoryConstants.CMIS.ITEM_ID_FQDN.equals(fqdnRepoFieldName)) {
            fieldDefinition = profileDefinition.getId();
        } else if (RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN.equals(fqdnRepoFieldName)) {
            fieldDefinition = profileDefinition.getLabel();
        } else {
            fieldDefinition = profileDefinition.getMetadata().get(contentApiFieldName);
        }
        return fieldDefinition;
    }
}