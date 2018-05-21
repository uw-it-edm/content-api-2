package edu.uw.edm.contentapi2.repository.acs.transformer;

import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import edu.uw.edm.contentapi2.controller.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.Constants;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
public class CMISDocumentConverterTest {

    private CMISDocumentConverter converter;


    @Before
    public void setUp() {
        converter = new CMISDocumentConverter();
    }

    @Test
    public void toContentApiDocument() {
        org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        Property propertyMock = mock(Property.class);
        when(propertyMock.getLocalName()).thenReturn("property1");
        when(propertyMock.getValue()).thenReturn("value1");

        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getName()).thenReturn("doc name");
        when(repositoryDocumentMock.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.singletonList(propertyMock));


        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("property1"), is(equalTo("value1")));
    }

}