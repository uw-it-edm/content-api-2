package edu.uw.edm.contentapi2.service;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;

import edu.uw.edm.contentapi2.config.RetryConfig;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.exceptions.ResourceNotFoundException;
import edu.uw.edm.contentapi2.security.User;

import static edu.uw.edm.contentapi2.service.PublicationService.CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME;
import static edu.uw.edm.contentapi2.service.PublicationService.CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 11/5/18
 */
public class PublicationServiceTest {

    private PublicationService publicationService;

    private DocumentFacade documentFacade;

    User mockUser;

    @Before
    public void setUp() {
        RetryTemplate retryTemplate = new RetryConfig().retryTemplate();

        documentFacade = mock(DocumentFacade.class);

        mockUser = mock(User.class);
        publicationService = new PublicationService(documentFacade, retryTemplate);
    }


    @Test(expected = ResourceNotFoundException.class)
    public void when_noDocuments_then_no_update_test() throws RepositoryException {

        SearchQueryModel searchQueryModel = new SearchQueryModel();

        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME, "clientProcessDefinitionKey", false));
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME, "clientProcessDefinitionKey", false));

        SearchResultContainer emptySearchResults = new SearchResultContainer();

        emptySearchResults.setTotalCount(0);

        when(documentFacade.searchDocuments(eq("test-profile"), eq(searchQueryModel), eq(mockUser)))
                .thenReturn(emptySearchResults);


        publicationService.updatePublication(new HashMap<>(), "test-profile", "clientProcessDefinitionKey", "clientProcessDefinitionKey", mockUser);

        verify(documentFacade, times(0)).updateDocument(any(), any(), eq(mockUser));
    }


    @Test
    public void when_2firstUpdateFails_then_retry_test() throws RepositoryException {

        SearchQueryModel searchQueryModel = new SearchQueryModel();

        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME, "clientProcessDefinitionKey", false));
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME, "clientProcessDefinitionKey", false));

        SearchResultContainer searchResultContainer = new SearchResultContainer();

        searchResultContainer.setTotalCount(1);
        SearchResult searchResult = new SearchResult();
        ContentAPIDocument document = new ContentAPIDocument();
        document.setId("test-id");

        searchResult.setDocument(document);
        searchResultContainer.getSearchResults().add(searchResult);

        when(documentFacade.searchDocuments(eq("test-profile"), eq(searchQueryModel), eq(mockUser)))
                .thenReturn(searchResultContainer);



        when(documentFacade.updateDocument(eq("test-id"), any(ContentAPIDocument.class), eq(mockUser)))
                .thenThrow(new RepositoryException())
                .thenThrow(new RepositoryException())
                .thenReturn(document);

        publicationService.updatePublication(new HashMap<>(), "test-profile", "clientProcessDefinitionKey", "clientProcessDefinitionKey", mockUser);

        verify(documentFacade, times(3)).updateDocument(any(), any(), eq(mockUser));
    }

    @Test
    public void when_multipleResults_then_multipleUpdates_test() throws RepositoryException {

        SearchQueryModel searchQueryModel = new SearchQueryModel();

        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME, "clientProcessDefinitionKey", false));
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME, "clientProcessDefinitionKey", false));

        SearchResultContainer searchResultContainer = new SearchResultContainer();

        searchResultContainer.setTotalCount(2);
        SearchResult searchResult1 = new SearchResult();
        SearchResult searchResult2 = new SearchResult();
        ContentAPIDocument document1 = new ContentAPIDocument();
        document1.setId("test-id");
        ContentAPIDocument document2 = new ContentAPIDocument();
        document2.setId("test-id-2");

        searchResult1.setDocument(document1);
        searchResult2.setDocument(document2);
        searchResultContainer.getSearchResults().add(searchResult1);
        searchResultContainer.getSearchResults().add(searchResult2);

        when(documentFacade.searchDocuments(eq("test-profile"), eq(searchQueryModel), eq(mockUser)))
                .thenReturn(searchResultContainer);


        when(documentFacade.updateDocument(eq("test-id"), any(ContentAPIDocument.class), eq(mockUser)))
                .thenReturn(document1);
        when(documentFacade.updateDocument(eq("test-id-2"), any(ContentAPIDocument.class), eq(mockUser)))
                .thenReturn(document2);

        publicationService.updatePublication(new HashMap<>(), "test-profile", "clientProcessDefinitionKey", "clientProcessDefinitionKey", mockUser);

        verify(documentFacade, times(2)).updateDocument(any(), any(), eq(mockUser));
    }

    @Test
    public void when_publication_then_documentUpdated_test() throws RepositoryException {

        SearchQueryModel searchQueryModel = new SearchQueryModel();

        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME, "clientProcessDefinitionKey", false));
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME, "clientProcessDefinitionKey", false));

        SearchResultContainer searchResultContainer = new SearchResultContainer();

        searchResultContainer.setTotalCount(2);
        SearchResult searchResult1 = new SearchResult();
        ContentAPIDocument document1 = new ContentAPIDocument();
        document1.setId("test-id");

        searchResult1.setDocument(document1);
        searchResultContainer.getSearchResults().add(searchResult1);


        when(documentFacade.searchDocuments(eq("test-profile"), eq(searchQueryModel), eq(mockUser)))
                .thenReturn(searchResultContainer);
        when(documentFacade.updateDocument(eq("test-id"), any(ContentAPIDocument.class), eq(mockUser)))
                .thenReturn(document1);


        HashMap<String, String> allRequestParams = new HashMap<>();
        allRequestParams.put("MyField", "MyValue");

        publicationService.updatePublication(allRequestParams, "test-profile", "clientProcessDefinitionKey", "clientProcessDefinitionKey", mockUser);



        ArgumentCaptor<ContentAPIDocument> updatedDocumentCaptor = ArgumentCaptor.forClass(ContentAPIDocument.class);
        verify(documentFacade).updateDocument(eq("test-id"), updatedDocumentCaptor.capture(), eq(mockUser));

        ContentAPIDocument updatedDocument = updatedDocumentCaptor.getValue();

        assertThat(updatedDocument.getId(),is(equalTo("test-id")));
        assertThat(updatedDocument.getMetadata().get("MyField"),is(equalTo("MyValue")));
        assertThat(updatedDocument.getMetadata().get("PublishStatus"),is(equalTo("Published")));
    }
}