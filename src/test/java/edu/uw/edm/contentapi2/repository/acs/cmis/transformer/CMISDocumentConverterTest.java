package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    public void toContentApiDocument() {
        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        Property propertyMock = mock(Property.class);
        when(propertyMock.getLocalName()).thenReturn("property1");
        when(propertyMock.getValue()).thenReturn("value1");

        when(repositoryDocumentMock.getId()).thenReturn("doc-id");

        when(repositoryDocumentMock.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.singletonList(propertyMock));

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        when(profileFacade.convertToContentApiFieldFromRepositoryField(anyString(), eq("property1"))).thenReturn("property1");

        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("property1"), is(equalTo("value1")));
    }

    @Test
    public void versionIsRemovedFromIdTest() {

        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.emptyList());

        when(repositoryDocumentMock.getId()).thenReturn("doc-id;1.0");
        when(repositoryDocumentMock.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);


        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }


    @Test
    public void idDoesntRequireVersionTest() {

        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.emptyList());

        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn("my:doctype");
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);

        
        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }

}