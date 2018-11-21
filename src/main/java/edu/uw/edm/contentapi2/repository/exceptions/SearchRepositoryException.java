package edu.uw.edm.contentapi2.repository.exceptions;

import okhttp3.ResponseBody;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
public class SearchRepositoryException extends RepositoryException {

    private String errorBody;

    public SearchRepositoryException() {
        super();
    }

    public SearchRepositoryException(String message) {
        super(message);
    }


    public SearchRepositoryException(String message, String errorBody) {
        super(message);
        this.errorBody = errorBody;
    }

    public SearchRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchRepositoryException(Throwable cause) {
        super(cause);
    }


    public String getErrorBody() {
        return errorBody;
    }
}
