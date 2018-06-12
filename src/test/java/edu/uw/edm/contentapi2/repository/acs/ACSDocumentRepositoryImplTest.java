package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.properties.ProfileProperties;
import edu.uw.edm.contentapi2.repository.acs.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.security.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet
 * Date: 4/4/18
 */
public class ACSDocumentRepositoryImplTest {


    Session mockSession;

    ACSDocumentRepositoryImpl documentRepository;

    @Mock
    ACSProfileRepository profileRepository;

    @Before
    public void setUp() {
        ACSSessionCreator sessionCreator = mock(ACSSessionCreator.class);

        mockSession = mock(Session.class);
        when(sessionCreator.getSessionForUser(any(User.class))).thenReturn(mockSession);

        documentRepository = new ACSDocumentRepositoryImpl(sessionCreator, new ACSProperties(), new ProfileProperties(), profileRepository);
    }

    @Test
    public void cmisGetByIdShouldBeCalledTest() throws NotADocumentException {

        when(mockSession.getObject("my-id")).thenReturn(mock(Document.class));

        documentRepository.getDocumentById("my-id", mock(User.class));

        verify(mockSession, times(1)).getObject("my-id");

    }

}