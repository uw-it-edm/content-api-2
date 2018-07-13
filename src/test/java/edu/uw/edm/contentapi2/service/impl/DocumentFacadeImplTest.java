package edu.uw.edm.contentapi2.service.impl;

import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet
 * Date: 4/4/18
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentFacadeImplTest {

    @Mock
    ExternalDocumentRepository repository;

    @Mock
    ExternalDocumentConverter converter;
    @Mock
    ExternalSearchDocumentRepository searchRepository;


    DocumentFacadeImpl documentFacade;

    @Before
    public void setUp() {
        documentFacade = new DocumentFacadeImpl(repository, searchRepository, converter);
    }

    @Test
    public void repositoryIsCalledWithCorrectId() throws RepositoryException {


        User mockUser = mock(User.class);
        when(repository.getDocumentById(any(), any())).thenReturn(mock(Document.class));

        documentFacade.getDocumentById("doc-id", mockUser);

        verify(repository, times(1)).getDocumentById(eq("doc-id"), eq(mockUser));
        verify(converter, times(1)).toContentApiDocument(any(), any());
    }
}