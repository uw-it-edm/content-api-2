package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Object convertToContentApiDataType(String profileId, User user, String fqdnRepoFieldName, Object value) throws NoSuchProfileException, UndefinedFieldException {
        final ProfileDefinitionV4 profileDefinition = getProfileDefinition(profileId, user);
        final String contentApiFieldName = convertToContentApiFieldFromFQDNRepositoryField(profileId, fqdnRepoFieldName);

        final FieldDefinition fieldDefinition = getFieldDefinition(profileDefinition, fqdnRepoFieldName, contentApiFieldName);
        if(fieldDefinition == null){
            Metrics.counter("edm.repo.field.undefined", Tags.of(Tag.of("profile", profileId), Tag.of("field", fqdnRepoFieldName)) ).increment();
            throw new UndefinedFieldException("Unable to determine FieldDefinition for '"+fqdnRepoFieldName+"' in profile '"+profileId+"'");
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
        checkNotNull(fieldDefinition,"Unable to determine FieldDefinition for '%s' in profile '%s'", contentApiFieldName,profileId);

        switch (fieldDefinition.getType()) {
            case date:
                value = DataTypeUtils.convertToLosAngelesDate(value);
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
    
    private FieldDefinition getFieldDefinition(ProfileDefinitionV4 profileDefinition, String fqdnRepoFieldName, String contentApiFieldName){
        FieldDefinition fieldDefinition;
        if(RepositoryConstants.Alfresco.AlfrescoFields.ITEM_ID_FQDN.equals(fqdnRepoFieldName)){
            fieldDefinition = profileDefinition.getId();
        }else if(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN.equals(fqdnRepoFieldName)){
            fieldDefinition = profileDefinition.getLabel();
        }else{
            fieldDefinition = profileDefinition.getMetadata().get(contentApiFieldName);
        }
        return  fieldDefinition;
    }
}