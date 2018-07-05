package edu.uw.edm.contentapi2.repository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NoSuchFieldException extends RepositoryException {
    public NoSuchFieldException(String fieldName) {
        super(fieldName);

    }
}
