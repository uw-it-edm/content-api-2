package edu.uw.edm.contentapi2.service;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

public interface ProfileDefinitionService {
    ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException;
}
