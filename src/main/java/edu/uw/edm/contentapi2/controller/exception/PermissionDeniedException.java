package edu.uw.edm.contentapi2.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Maxime Deravet Date: 11/20/18
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends ResponseStatusException {
    public PermissionDeniedException(String user) {
        super(HttpStatus.FORBIDDEN, user + " is not authorized to access resource");
    }
}
