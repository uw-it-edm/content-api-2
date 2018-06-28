package edu.uw.edm.contentapi2.service.profile;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

/**
 * @author Maxime Deravet Date: 6/28/18
 */
public interface ProfileDefinitionService {

    ProfileDefinitionV4 getProfileDefinition(String profileId, User user) throws NoSuchProfileException;
}
