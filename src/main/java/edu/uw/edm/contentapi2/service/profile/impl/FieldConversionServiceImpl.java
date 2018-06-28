package edu.uw.edm.contentapi2.service.profile.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.FieldConversionService;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Maxime Deravet Date: 6/28/18
 */
@Service
@Slf4j
public class FieldConversionServiceImpl implements FieldConversionService {

    public static final List<String> ID_AND_LABEL_FIELDS = Arrays.asList(Constants.ContentAPI.LABEL, Constants.ContentAPI.ID);
    private ProfileDefinitionService profileDefinitionService;

    @Autowired
    public FieldConversionServiceImpl(ProfileDefinitionService profileDefinitionService) {
        this.profileDefinitionService = profileDefinitionService;
    }


    @Override
    @Cacheable(value = "profile-content-to-fqdn-field", key = "{#contentFieldName, #profileId, #user.username}")
    public String getRepoFQDNFieldName(String contentFieldName, String profileId, User user) throws NoSuchProfileException {
        log.trace("not hitting getRepoFQDNFieldName cache for {} {} {}", contentFieldName, profileId, user.getUsername());

        ProfileDefinitionV4 profileDefinition = profileDefinitionService.getProfileDefinition(profileId, user);

        if (idOrLabel(contentFieldName)) {
            return getRepoNameForIdOrLabel(contentFieldName, profileId, user);
        } else {
            FieldDefinition fieldDefinition = profileDefinition.getMetadata().get(contentFieldName);
            return fieldDefinition != null ? fieldDefinition.getRepoFieldName() : contentFieldName;
        }

    }

    private String getRepoNameForIdOrLabel(String contentFieldName, String profileId, User user) throws NoSuchProfileException {
        checkArgument(ID_AND_LABEL_FIELDS.contains(contentFieldName), "getRepoNameForIdOrLabel() only handle id and label");

        ProfileDefinitionV4 profileDefinition = profileDefinitionService.getProfileDefinition(profileId, user);
        switch (contentFieldName) {
            case Constants.ContentAPI.ID:
                return profileDefinition.getId().getRepoFieldName();
            case Constants.ContentAPI.LABEL:
                return profileDefinition.getLabel().getRepoFieldName();
            default:
                throw new AssertionError("getRepoNameForIdOrLabel() only handle id and label");
        }

    }

    private boolean idOrLabel(String contentFieldName) {
        return ID_AND_LABEL_FIELDS.contains(contentFieldName);
    }
}
