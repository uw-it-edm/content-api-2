package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.exceptions.UnknownFieldDefinitionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    public void toContentApiDocument() throws NoSuchProfileException, UnknownFieldDefinitionException {
        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        final Property propertyMock = mock(Property.class);
        when(propertyMock.getLocalName()).thenReturn("property1");
        when(propertyMock.getId()).thenReturn("my:property1");
        when(propertyMock.getValue()).thenReturn("value1");

        when(repositoryDocumentMock.getId()).thenReturn("doc-id");

        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.singletonList(propertyMock));

        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        when(profileFacade.convertToContentApiFieldFromRepositoryField(anyString(), eq("property1"))).thenReturn("property1");
        when(profileFacade.convertToContentApiDataType(anyString(), any(User.class), eq("my:property1"), eq("value1"))).thenReturn("value1");
        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("property1"), is(equalTo("value1")));
        assertThat("metadata.ProfileId", contentAPIDocument.getMetadata().get(RepositoryConstants.ContentAPI.PROFILE_ID), is(equalTo("my:doctype")));
    }

    @Test
    public void versionIsRemovedFromIdTest() throws NoSuchProfileException {
        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.emptyList());

        when(repositoryDocumentMock.getId()).thenReturn("doc-id;1.0");
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }


    @Test
    public void idDoesntRequireVersionTest() throws NoSuchProfileException {

        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.emptyList());

        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);


        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }

    @Test
    public void sysPrefixPropertiesAreRemovedFromDocumentTest() throws NoSuchProfileException, UnknownFieldDefinitionException {
        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.emptyList());

        Property<?> sysProperty = mock(Property.class);
        when(sysProperty.getId()).thenReturn("sys:testSysProperty");
        List<Property<?>> propertyList = new ArrayList<>();
        propertyList.add(sysProperty);
        when(repositoryDocumentMock.getProperties()).thenReturn(propertyList);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id");

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        verify(profileFacade, never()).convertToContentApiFieldFromRepositoryField(any(), any());
        verify(profileFacade, never()).convertToContentApiDataType(any(), any(), any(), any());

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertEquals(1, contentAPIDocument.getMetadata().size());
    }
}