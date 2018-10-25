package edu.uw.edm.contentapi2.repository.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NotADocumentException extends RepositoryException {
}
