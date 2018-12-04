package edu.uw.edm.contentapi2.service.impl;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import edu.uw.edm.contentapi2.TestUtilities;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ProfiledSearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentFacadeImplTest {

    @Mock
    private ExternalDocumentRepository repository;

    @Mock
    private ExternalDocumentConverter converter;
    @Mock
    private  ExternalSearchDocumentRepository searchRepository;
    @Mock
    private User user;

    private  DocumentFacadeImpl documentFacade;

    @Before
    public void setUp() {
        documentFacade = new DocumentFacadeImpl(repository, searchRepository, converter);
    }

    @Test
    public void when_delete_then_repositoryIsCalledWithCorrectIdTest() throws RepositoryException {

        User mockUser = mock(User.class);

        documentFacade.deleteDocumentById("doc-id", mockUser);

        verify(repository, times(1)).deleteDocumentById(eq("doc-id"), eq(mockUser));
    }

    @Test
    public void when_get_then_repositoryIsCalledWithCorrectIdTest() throws RepositoryException {
        User mockUser = mock(User.class);
        when(repository.getDocumentById(any(), any())).thenReturn(mock(Document.class));

        documentFacade.getDocumentById("doc-id", mockUser);

        verify(repository, times(1)).getDocumentById(eq("doc-id"), eq(mockUser));
        verify(converter, times(1)).toContentApiDocument(any(), any());
    }

    @Test
    public void legacySearchDocumentsExactId() throws RepositoryException {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();
        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID +"=test-profile");
        searches.add("testId=123");
        legacySearchModel.setSearch(searches);


        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", false));

        when(searchRepository.searchDocuments(eq("test-profile"), eq(expectedSearchQueryModel), any(User.class))).thenReturn(getTestSearchResultContainer("123"));
        final DocumentSearchResults documentSearchResults = documentFacade.searchDocuments(legacySearchModel, user);

        assertEquals(1,documentSearchResults.getResultListSize());
        assertEquals(100,documentSearchResults.getTotalCount());

        final ContentAPIDocument resultDoc = documentSearchResults.getDocuments().toArray(new ContentAPIDocument[10])[0];
        assertEquals("123",resultDoc.getId());
        assertEquals("The Profile of the document",resultDoc.getProfileId());

        verify(searchRepository, times(1)).searchDocuments(eq("test-profile"), eq(expectedSearchQueryModel), any(User.class));

    }

    private SearchResultContainer getTestSearchResultContainer(String itemId) {
        final SearchResultContainer searchResultContainer = new SearchResultContainer();
        searchResultContainer.getSearchResults().add(getTestSearchResult(itemId));
        searchResultContainer.setTotalCount(100L);
        return searchResultContainer;

    }

    private SearchResult getTestSearchResult(String itemId) {
        final SearchResult searchResult = new SearchResult();
        searchResult.setDocument(TestUtilities.getTestDocument(itemId));
        return searchResult;
    }
}