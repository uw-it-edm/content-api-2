package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.Constants;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
@RunWith(MockitoJUnitRunner.class)
public class CMISDocumentConverterTest {

    private CMISDocumentConverter converter;
    private org.apache.chemistry.opencmis.client.api.Document repositoryDocumentMock;
    @Mock
    FieldMapper fieldMapper;

    @Before
    public void setUp() {
        converter = new CMISDocumentConverter(fieldMapper);


        repositoryDocumentMock = mock(org.apache.chemistry.opencmis.client.api.Document.class);
        final Property propertyMock = mock(Property.class);
        when(propertyMock.getLocalName()).thenReturn("property1");
        when(propertyMock.getValue()).thenReturn("value1");

        final DocumentType mockDocumentType = mock(DocumentType.class);
        when(mockDocumentType.getLocalName()).thenReturn("doc-site");

        when(repositoryDocumentMock.getDocumentType()).thenReturn(mockDocumentType);
        when(repositoryDocumentMock.getId()).thenReturn("doc-id");
        when(repositoryDocumentMock.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN)).thenReturn("doc name");
        when(repositoryDocumentMock.getProperties()).thenReturn(Collections.singletonList(propertyMock));

    }

    @Test
    public void toContentApiDocument() {
        when(fieldMapper.convertToContentApiFieldFromRepositoryField(anyString(), anyString())).thenAnswer(i -> i.getArguments()[1]);//return second argument

        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("property1"), is(equalTo("value1")));
    }

    @Test
    public void toContentApiDocumentWithFieldOverride() {
        when(fieldMapper.convertToContentApiFieldFromRepositoryField(anyString(), anyString())).thenReturn("overridenName");

        ContentAPIDocument contentAPIDocument = converter.toContentApiDocument(repositoryDocumentMock);

        assertThat("docId", contentAPIDocument.getId(), is(equalTo("doc-id")));
        assertThat("label", contentAPIDocument.getLabel(), is(equalTo("doc name")));
        assertThat("metadata.Property1", contentAPIDocument.getMetadata().get("overridenName"), is(equalTo("value1")));
    }

}