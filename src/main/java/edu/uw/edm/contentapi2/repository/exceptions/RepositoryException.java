package edu.uw.edm.contentapi2.repository.exceptions;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public class RepositoryException extends Exception {
    public RepositoryException() {
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }
}
