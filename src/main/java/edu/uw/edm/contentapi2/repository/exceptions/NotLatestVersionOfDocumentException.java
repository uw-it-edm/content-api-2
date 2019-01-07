package edu.uw.edm.contentapi2.repository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class NotLatestVersionOfDocumentException extends RepositoryException {
    public NotLatestVersionOfDocumentException(String message) {
        super(message);
    }

    public NotLatestVersionOfDocumentException() {
        super();
    }
}
