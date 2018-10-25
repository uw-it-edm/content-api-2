package edu.uw.edm.contentapi2.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import edu.uw.edm.contentapi2.common.impl.YamlFieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.BulkV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentsUpdateResult;
import edu.uw.edm.contentapi2.properties.ContentApiProperties;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BulkV3ControllerTest {

    @MockBean
    private DocumentFacade documentFacade;
    @MockBean
    private ContentApiProperties contentApiProperties;
    @MockBean
    private YamlFieldMapper yamlFieldMapper;

    @Mock
    private User user;


    private BulkV3Controller controller;

    @Before
    public void setUp() throws Exception {
        when(contentApiProperties.getBulkUpdateMaxItems()).thenReturn(10);
        controller = new BulkV3Controller(contentApiProperties, documentFacade);

        when(documentFacade.updateDocument(startsWith("success-id"), any(ContentAPIDocument.class), any(User.class))).thenAnswer(i -> i.getArguments()[1]);//return second argument
        when(documentFacade.updateDocument(startsWith("failed-id"), any(ContentAPIDocument.class), any(User.class))).thenThrow(new RepositoryException("Test Exception Message"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void whenUpdateItemsWithZeroDocumentsThrowIllegalArgumentException() {
        final List<ContentAPIDocument> documents = new ArrayList<>();
        controller.updateItems(documents, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenUpdateItemsWithTooManyDocumentsThrowIllegalArgumentException() {
        when(contentApiProperties.getBulkUpdateMaxItems()).thenReturn(1);
        final List<ContentAPIDocument> documents = new ArrayList<>();
        documents.add(new ContentAPIDocument());
        documents.add(new ContentAPIDocument());
        controller.updateItems(documents, user);
    }

    @Test
    public void updateItemsWithOneSuccess() throws Exception {
        final ContentAPIDocument successfulDoc = new ContentAPIDocument();
        successfulDoc.setId("success-id-1");
        final List<ContentAPIDocument> documents = new ArrayList<>();
        documents.add(successfulDoc);
        final ResponseEntity<DocumentsUpdateResult> result = controller.updateItems(documents, user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getSuccesses().size());
        assertEquals(0, result.getBody().getFailures().size());
        assertEquals(successfulDoc, result.getBody().getSuccesses().get(0));

        verify(documentFacade, times(1)).updateDocument(startsWith("success-id"), any(ContentAPIDocument.class), any(User.class));
    }

    @Test
    public void updateItemsWithMultipleSuccess() throws Exception {
        final ContentAPIDocument successfulDoc1 = new ContentAPIDocument();
        successfulDoc1.setId("success-id-1");
        final ContentAPIDocument successfulDoc2 = new ContentAPIDocument();
        successfulDoc2.setId("success-id-2");
        final List<ContentAPIDocument> documents = new ArrayList<>();
        documents.add(successfulDoc1);
        documents.add(successfulDoc2);
        final ResponseEntity<DocumentsUpdateResult> result = controller.updateItems(documents, user);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().getSuccesses().size());
        assertEquals(successfulDoc1, result.getBody().getSuccesses().get(0));
        assertEquals(successfulDoc2, result.getBody().getSuccesses().get(1));

        assertEquals(0, result.getBody().getFailures().size());

        verify(documentFacade, times(2)).updateDocument(startsWith("success-id"), any(ContentAPIDocument.class), any(User.class));
    }


    @Test
    public void updateItemsWithFailedUpdatePartialSuccess() throws Exception {
        final ContentAPIDocument failedDoc1 = new ContentAPIDocument();
        failedDoc1.setId("failed-id-1");
        final ContentAPIDocument successfulDoc2 = new ContentAPIDocument();
        successfulDoc2.setId("success-id-2");
        final List<ContentAPIDocument> documents = new ArrayList<>();
        documents.add(failedDoc1);
        documents.add(successfulDoc2);
        final ResponseEntity<DocumentsUpdateResult> result = controller.updateItems(documents, user);

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertEquals(1, result.getBody().getSuccesses().size());
        assertEquals(successfulDoc2, result.getBody().getSuccesses().get(0));

        assertEquals(1, result.getBody().getFailures().size());
        assertEquals(failedDoc1, result.getBody().getFailures().get(0));
        assertEquals(RepositoryException.class, result.getBody().getFailures().get(0).getException());
        assertEquals("Test Exception Message", result.getBody().getFailures().get(0).getError());

        verify(documentFacade, times(1)).updateDocument(startsWith("failed-id"), any(ContentAPIDocument.class), any(User.class));
        verify(documentFacade, times(1)).updateDocument(startsWith("success-id"), any(ContentAPIDocument.class), any(User.class));

    }

    @Test
    public void updateItemsWithFailedUpdateNoSuccess() throws Exception {
        final ContentAPIDocument failedDoc1 = new ContentAPIDocument();
        failedDoc1.setId("failed-id-1");
        final List<ContentAPIDocument> documents = new ArrayList<>();
        documents.add(failedDoc1);
        final ResponseEntity<DocumentsUpdateResult> result = controller.updateItems(documents, user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(0, result.getBody().getSuccesses().size());
        assertEquals(1, result.getBody().getFailures().size());
        assertEquals(failedDoc1, result.getBody().getFailures().get(0));
        assertEquals(RepositoryException.class, result.getBody().getFailures().get(0).getException());
        assertEquals("Test Exception Message", result.getBody().getFailures().get(0).getError());

        verify(documentFacade, times(1)).updateDocument(startsWith("failed-id"), any(ContentAPIDocument.class), any(User.class));

    }
}
