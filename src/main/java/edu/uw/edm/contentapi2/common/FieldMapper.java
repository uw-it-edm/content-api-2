package edu.uw.edm.contentapi2.common;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;

public interface FieldMapper {
    String getContentTypeForProfile(String profileId) throws NoSuchProfileException;

    String convertToContentApiFieldFromRepositoryField(String profile, String repoFieldLocalName);
}
