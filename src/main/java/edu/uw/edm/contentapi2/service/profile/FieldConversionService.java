package edu.uw.edm.contentapi2.service.profile;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 6/28/18
 */
public interface FieldConversionService {

    String getRepoFQDNFieldName(String contentFieldName, String profileId, User user) throws NoSuchProfileException;
}
