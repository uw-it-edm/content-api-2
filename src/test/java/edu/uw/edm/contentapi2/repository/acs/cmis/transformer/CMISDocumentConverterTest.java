package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Rendition;
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
@RunWith(MockitoJUnitRunner.Silent.class)
public class CMISDocumentConverterTest {

    @Mock
    ProfileFacade profileFacade;

    private CMISDocumentConverter converter;


    @Before
    public void setUp() {
        converter = new CMISDocumentConverter(profileFacade);
    }

    @Test
    public void whenNoRenditionsThenWebExtensionIsFilenameExtensionTest() throws NoSuchProfileException {
        Document docId = getMockDocument("docId", "my:doctype");

        when(docId.getRenditions()).thenReturn(null);
        when(docId.getContentStreamFileName()).thenReturn("myfile.txt");
        when(docId.getContentStreamLength()).thenReturn(123L);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(docId, mock(User.class));

        assertThat("WebExtension", contentAPIDocument.getMetadata().get("WebExtension"), is("txt"));
        assertThat("FileSize", contentAPIDocument.getMetadata().get("FileSize"), is(123L));

    }

    @Test
    public void whenPDFRenditionsThenWebExtensionIsPDFTest() throws NoSuchProfileException {
        Document docId = getMockDocument("docId", "my:doctype");

        List<Rendition> renditions = new ArrayList<>();
        Rendition pdfRendition = mock(Rendition.class);

        when(pdfRendition.getKind()).thenReturn("pdf");
        when(pdfRendition.getLength()).thenReturn(1234L);
        renditions.add(pdfRendition);


        when(docId.getRenditions()).thenReturn(renditions);
        when(docId.getContentStreamFileName()).thenReturn("myfile.txt");
        when(docId.getContentStreamLength()).thenReturn(123L);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(docId, mock(User.class));

        assertThat("WebExtension", contentAPIDocument.getMetadata().get("WebExtension"), is("pdf"));
        assertThat("FileSize", contentAPIDocument.getMetadata().get("FileSize"), is(1234L));

    }

    @Test
    public void whenMultiplePDFRenditionsThenFirstIsUsedTest() throws NoSuchProfileException {
        Document docId = getMockDocument("docId", "my:doctype");

        List<Rendition> renditions = new ArrayList<>();
        Rendition pdfRendition = mock(Rendition.class);

        when(pdfRendition.getKind()).thenReturn("pdf");
        when(pdfRendition.getLength()).thenReturn(1234L);
        renditions.add(pdfRendition);

        Rendition pdfRendition2 = mock(Rendition.class);

        when(pdfRendition2.getKind()).thenReturn("pdf");
        when(pdfRendition2.getLength()).thenReturn(456L);
        renditions.add(pdfRendition2);


        when(docId.getRenditions()).thenReturn(renditions);
        when(docId.getContentStreamFileName()).thenReturn("myfile.txt");
        when(docId.getContentStreamLength()).thenReturn(123L);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(docId, mock(User.class));

        assertThat("WebExtension", contentAPIDocument.getMetadata().get("WebExtension"), is("pdf"));
        assertThat("FileSize", contentAPIDocument.getMetadata().get("FileSize"), is(1234L));

    }

    @Test
    public void whenMultipleRenditionsThePDFIsUsedTest() throws NoSuchProfileException {
        Document docId = getMockDocument("docId", "my:doctype");

        List<Rendition> renditions = new ArrayList<>();
        Rendition docLibRendition = mock(Rendition.class);

        when(docLibRendition.getKind()).thenReturn("doclib");
        when(docLibRendition.getLength()).thenReturn(1234L);
        renditions.add(docLibRendition);

        Rendition pdfRendition2 = mock(Rendition.class);

        when(pdfRendition2.getKind()).thenReturn("pdf");
        when(pdfRendition2.getLength()).thenReturn(456L);
        renditions.add(pdfRendition2);


        when(docId.getRenditions()).thenReturn(renditions);
        when(docId.getContentStreamFileName()).thenReturn("myfile.txt");
        when(docId.getContentStreamLength()).thenReturn(123L);

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(docId, mock(User.class));

        assertThat("WebExtension", contentAPIDocument.getMetadata().get("WebExtension"), is("pdf"));
        assertThat("FileSize", contentAPIDocument.getMetadata().get("FileSize"), is(456L));

    }

    @Test
    public void toContentApiDocumentTest() throws NoSuchProfileException {
        final Document repositoryDocumentMock = getMockDocument("doc-id", "doctype");

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
        final Document repositoryDocumentMock = getMockDocument("doc-id;1.0", "my:doctype");

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }

    @Test
    public void idDoesntRequireVersionTest() throws NoSuchProfileException {
        final Document repositoryDocumentMock = getMockDocument("doc-id", "my:doctype");

        final ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock, mock(User.class));

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
    }

    @Test
    public void sysPrefixPropertiesAreRemovedFromDocumentTest() throws NoSuchProfileException, UndefinedFieldException {
        final Document repositoryDocumentMock = mock(Document.class);
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

        //ProfileId and FileSize
        assertEquals(2, contentAPIDocument.getMetadata().size());
    }

    private Document getMockDocument(String docId, String docType) {
        final Document repositoryDocumentMock = mock(Document.class);
        when(repositoryDocumentMock.getId()).thenReturn(docId);
        when(repositoryDocumentMock.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");

        final DocumentType documentTypeMock = mock(DocumentType.class);
        when(documentTypeMock.getLocalName()).thenReturn(docType);
        when(repositoryDocumentMock.getDocumentType()).thenReturn(documentTypeMock);
        return repositoryDocumentMock;
    }

}