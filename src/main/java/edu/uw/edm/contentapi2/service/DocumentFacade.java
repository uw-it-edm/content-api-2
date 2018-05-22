package edu.uw.edm.contentapi2.service;

import org.springframework.web.multipart.MultipartFile;

import edu.uw.edm.contentapi2.controller.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface DocumentFacade {

    ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException;

    ContentAPIDocument createDocument(ContentAPIDocument contentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException;

    ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException;
}
