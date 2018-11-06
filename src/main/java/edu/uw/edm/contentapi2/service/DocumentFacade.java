package edu.uw.edm.contentapi2.service;

import org.springframework.web.multipart.MultipartFile;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
public interface DocumentFacade {

    ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException;

    ContentAPIDocument createDocument(ContentAPIDocument contentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException;

    ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, User user) throws RepositoryException;

    ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException;

    DocumentSearchResults searchDocuments(LegacySearchModel legacySearchModel, User user) throws RepositoryException;

    SearchResultContainer searchDocuments(String indexName, SearchQueryModel searchQueryModel, User user) throws RepositoryException;

    void deleteDocumentById(String itemId, User user) throws RepositoryException;
}
