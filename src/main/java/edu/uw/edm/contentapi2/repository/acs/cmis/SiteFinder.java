package edu.uw.edm.contentapi2.repository.acs.cmis;

import org.apache.chemistry.opencmis.client.api.Folder;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 7/10/18
 */
public interface SiteFinder {
    Folder getSiteRootFolderForProfileId(String profileId, User user) throws NoSuchProfileException;

    Folder getSiteRootFolderFromContentApiDocument(ContentAPIDocument document, User user) throws NoSuchProfileException;
}
