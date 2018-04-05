package edu.uw.edm.contentapi2.repository;

import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface ExternalDocumentRepository<T> {
    T getDocumentById(String documentId, User user) throws RepositoryException;
}
