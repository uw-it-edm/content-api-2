package edu.uw.edm.contentapi2.repository;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchModel;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface ExternalDocumentRepository<T> {
    T getDocumentById(String documentId, User user, String renditionFilter) throws NotADocumentException;

    T getDocumentById(String documentId, User user) throws RepositoryException;

    T createDocument(ContentAPIDocument document, MultipartFile primaryFile, User user) throws RepositoryException;

    T updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException;

    Set<T> searchDocuments(SearchModel searchModel, User user) throws RepositoryException;
}
