package edu.uw.edm.contentapi2.repository.acs.cmis.impl;

import com.google.common.base.Strings;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.acs.cmis.SiteFinder;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet Date: 7/10/18
 */
@Service
@Slf4j
public class SiteFinderImpl implements SiteFinder {

    private static final String DOCUMENT_LIBRARY_PATH_TEMPLATE = "/Sites/%s/documentLibrary";

    private ACSSessionCreator sessionCreator;

    @Autowired
    public SiteFinderImpl(ACSSessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }

    /**
     * We get the root folder from the ContentAPI#PROFILE_ID metadata field
     */
    @Override
    @Cacheable(value = "site-finder", key = "{#document.getProfileId(), #user.username}")
    public Folder getSiteRootFolderFromContentApiDocument(ContentAPIDocument document, User user) throws NoSuchProfileException {
        checkNotNull(document, "document metadata shouldn't be null");
        checkNotNull(document.getMetadata(), "document should contain metadata");


        return getSiteRootFolderForProfileId(document.getProfileId(), user);

    }

    @Override
    @Cacheable(value = "site-finder", key = "{#profileId, #user.username}")
    public Folder getSiteRootFolderForProfileId(String profileId, User user) throws NoSuchProfileException {
        checkArgument(!Strings.isNullOrEmpty(profileId), "Should have a profileId field");

        Session session = sessionCreator.getSessionForUser(user);

        CmisObject documentLibraryFolderForProfile = session.getObjectByPath(getDocumentLibraryPath(profileId));

        if (documentLibraryFolderForProfile.getType() instanceof FolderType) {
            log.debug("using folder {} at path {}", documentLibraryFolderForProfile.getName(), ((Folder) documentLibraryFolderForProfile).getPath());
            return (Folder) documentLibraryFolderForProfile;
        } else {
            throw new NoSuchProfileException(profileId);
        }
    }

    private String getDocumentLibraryPath(String profileId) {
        return String.format(DOCUMENT_LIBRARY_PATH_TEMPLATE, profileId);
    }

}
