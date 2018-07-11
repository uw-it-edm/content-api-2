package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.FieldConversionService;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;
import edu.uw.edm.contentapi2.service.util.DataTypeUtils;
import lombok.extern.slf4j.Slf4j;

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
    public String convertToContentApiFieldFromFQDNRepositoryField(String profile, String repoFieldName) {
        String localName = Iterables.getLast(Splitter.on(':').split(repoFieldName));

        return fieldMapper.convertToContentApiFieldFromRepositoryField(profile, localName);
    }

    @Override
    public Object convertToContentApiDataType(String profileId, User user, String repoFieldLocalName, Object value) throws NoSuchProfileException {
        final ProfileDefinitionV4 profileDefinition = getProfileDefinition(profileId, user);
        final String contentApiFieldName = fieldMapper.convertToContentApiFieldFromRepositoryField(profileId, repoFieldLocalName);
        final FieldDefinition fieldDefinition = profileDefinition.getMetadata().get(contentApiFieldName);

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


}