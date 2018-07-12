package edu.uw.edm.contentapi2.repository.acs.cmis.impl;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.acs.cmis.SiteFinder;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 7/11/18
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteFinderImplTest {


    private SiteFinder siteFinder;

    private User testUser = new User("me", "", Collections.emptyList());

    @Mock
    ACSSessionCreator sessionCreator;
    @Mock
    Session sessionMock;

    @Before
    public void setUp() {

        when(sessionCreator.getSessionForUser(eq(testUser))).thenReturn(sessionMock);

        siteFinder = new SiteFinderImpl(sessionCreator);


    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNoProfileFieldThenIllegalArgumentExceptionTest() throws NoSuchProfileException {

        siteFinder.getSiteRootFolderFromContentApiDocument(new ContentAPIDocument(), testUser);
    }

    @Test
    public void sessionCreatorIsCalledTest() throws NoSuchProfileException {

        ContentAPIDocument document = new ContentAPIDocument();
        document.getMetadata().put(PROFILE_ID, "my-profile");


        Folder folderMock = mock(Folder.class);
        when(folderMock.getType()).thenReturn(mock(FolderType.class));

        when(sessionMock.getObjectByPath(eq("/Sites/my-profile/documentLibrary"))).thenReturn(folderMock);

       Folder folder =  siteFinder.getSiteRootFolderFromContentApiDocument(document, testUser);

        verify(sessionCreator, times(1)).getSessionForUser(eq(testUser));

        assertEquals(folder, folderMock);
    }

}