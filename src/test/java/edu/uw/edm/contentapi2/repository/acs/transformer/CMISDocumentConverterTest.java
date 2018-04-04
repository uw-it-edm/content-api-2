package edu.uw.edm.contentapi2.repository.acs.transformer;

import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import edu.uw.edm.contentapi2.controller.model.ContentAPIDocument;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet
 * Date: 4/4/18
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
        when(repositoryDocumentMock.getProperties()).thenReturn(Arrays.asList(propertyMock));


        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat(contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat(contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat(contentAPIDocument.getMetadata().get("property1"), is(equalTo("value1")));
    }

}