package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.repository.acs.cmis.ACSDocumentRepositoryImpl;
import edu.uw.edm.contentapi2.repository.acs.cmis.ACSProfileRepository;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static edu.uw.edm.contentapi2.repository.constants.Constants.ContentAPI.PROFILE_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
@RunWith(MockitoJUnitRunner.class)
public class ACSDocumentRepositoryImplTest {


    Session mockSession;

    ACSDocumentRepositoryImpl documentRepository;

    @Mock
    ACSProfileRepository profileRepository;
    @Mock
    ProfileFacade profileFacade;

    User testUser;

    @Before
    public void setUp() {
        ACSSessionCreator sessionCreator = mock(ACSSessionCreator.class);

        mockSession = mock(Session.class);
        when(sessionCreator.getSessionForUser(any(User.class))).thenReturn(mockSession);

        testUser = new User("test-user", "", Collections.emptyList());

        documentRepository = new ACSDocumentRepositoryImpl(sessionCreator, new ACSProperties(), profileRepository, profileFacade);
    }

    @Test
    public void cmisGetByIdShouldBeCalledTest() throws NotADocumentException {

        when(mockSession.getObject("my-id")).thenReturn(mock(Document.class));

        documentRepository.getDocumentById("my-id", mock(User.class));

        verify(mockSession, times(1)).getObject("my-id");

    }

    @Test
    public void getPropertyDefinition() {
        final Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<>();
        propertyDefinitions.put("test:trackingId", mock(PropertyDefinition.class));
        when(profileRepository.getPropertyDefinition(any(User.class), eq("test-content-type"))).thenReturn(propertyDefinitions);

        Map<String, PropertyDefinition<?>> results = documentRepository.getPropertyDefinition(testUser, "test-content-type");
        assertEquals(propertyDefinitions.size(), results.size());
        assertEquals(propertyDefinitions.get("test:trackingId"), results.get("test:trackingId"));
    }


    @Test
    public void createDocumentTest() throws NoSuchProfileException {
        when(profileFacade.convertToContentApiFieldFromRepositoryField(anyString(), anyString())).thenAnswer(i -> i.getArguments()[1]);//return second argument
        when(profileFacade.getContentTypeForProfile(any())).thenReturn("test:TestProfile");

        final Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<>();
        final PropertyDefinition mockPropertyDefinition = mock(PropertyDefinition.class);
        when(mockPropertyDefinition.getLocalName()).thenReturn("testKey");
        propertyDefinitions.put("test:testKey", mockPropertyDefinition);
        when(profileRepository.getPropertyDefinition(any(User.class), anyString())).thenReturn(propertyDefinitions);


        final Folder mockDocumentLibraryFolderForProfile = mock(Folder.class);
        when(mockDocumentLibraryFolderForProfile.getType()).thenReturn(mock(FolderType.class));
        when(mockDocumentLibraryFolderForProfile.getName()).thenReturn("mockLibraryFolder");


        final ObjectType mockTitled = mock(ObjectType.class);
        when(mockTitled.getPropertyDefinitions()).thenReturn(new HashMap<>());
        when(mockSession.getTypeDefinition(eq(Constants.Alfresco.AlfrescoAspects.TITLED))).thenReturn(mockTitled);
        when(mockSession.getObjectByPath(any())).thenReturn(mockDocumentLibraryFolderForProfile);

        final MultipartFile mockPrimaryFile = mock(MultipartFile.class);
        when(mockPrimaryFile.isEmpty()).thenReturn(true);
        when(mockPrimaryFile.getOriginalFilename()).thenReturn("mockFile");

        final ContentAPIDocument document = new ContentAPIDocument();
        document.setId("doc-id");
        document.setLabel("doc-label");
        final Map<String, Object> docMetadata = new HashMap<>();
        docMetadata.put(PROFILE_ID, "testProfile");
        docMetadata.put("testKey", "testValue");
        document.setMetadata(docMetadata);

        documentRepository.createDocument(document, mockPrimaryFile, mock(User.class));

        verify(mockDocumentLibraryFolderForProfile, times(1)).createDocument(any(), any(), any());
        //test that the user provided metadata is passed to the repository
        final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockDocumentLibraryFolderForProfile).createDocument(captor.capture(), any(), any());
        final Map<String, Object> createDocumentPropertiesParameter = captor.getValue();
        assertThat(createDocumentPropertiesParameter.size(), is(5));
        assertEquals(true, createDocumentPropertiesParameter.containsKey("test:testKey"));

    }
}