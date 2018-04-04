package edu.uw.edm.contentapi2.service;

import edu.uw.edm.contentapi2.controller.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface DocumentFacade {

    ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException;
}
