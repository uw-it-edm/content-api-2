package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.exceptions.UndefinedFieldException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
@RunWith(MockitoJUnitRunner.class)
public class CMISDocumentConverterTest {

    @Mock
    ProfileFacade profileFacade;

    private CMISDocumentConverter converter;


    @Before
    public void setUp() {
        converter = new CMISDocumentConverter(profileFacade);
    }

    @Test
    public void toContentApiDocument() throws NoSuchProfileException {
        final org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");


        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        final Map<String, Object> convertedMetaData = new HashMap<>();
        convertedMetaData.put("Property1", "value1");
        convertedMetaData.put(RepositoryConstants.ContentAPI.PROFILE_ID, "doctype");
        when(profileFacade.convertMetadataToContentApiDataTypes(any(Document.class), any(User.class), eq("doctype"))).thenReturn(convertedMetaData);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("Property1"), is(equalTo("value1")));
        assertThat("metadata.ProfileId", contentAPIDocument.getMetadata().get(RepositoryConstants.ContentAPI.PROFILE_ID), is(equalTo("doctype")));
    }

    @Test
    public void versionIsRemovedFromIdTest() throws NoSuchProfileException {
        final org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id;1.0");
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }


    @Test
    public void idDoesntRequireVersionTest() throws NoSuchProfileException {
        final org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }

    @Test
    public void sysPrefixPropertiesAreRemovedFromDocumentTest() throws NoSuchProfileException, UndefinedFieldException {
        final org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id");

        final Property<?> sysProperty = mock(Property.class);
        final List<Property<?>> propertyList = new ArrayList<>();
        propertyList.add(sysProperty);

        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        verify(profileFacade, never()).convertToContentApiFieldFromRepositoryField(any(), any());
        verify(profileFacade, never()).convertToContentApiDataType(any(), any(), any(), any());

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertEquals(1, contentAPIDocument.getMetadata().size());
    }
}