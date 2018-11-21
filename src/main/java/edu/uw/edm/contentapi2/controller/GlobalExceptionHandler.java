package edu.uw.edm.contentapi2.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.exception.PermissionDeniedException;
import edu.uw.edm.contentapi2.controller.exception.UnauthorizedException;
import edu.uw.edm.contentapi2.repository.acs.openapi.model.SearchRepositoryError;
import edu.uw.edm.contentapi2.repository.exceptions.SearchRepositoryException;

/**
 * @author Maxime Deravet Date: 11/20/18
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseStatusExceptionResolver {
    private ObjectMapper mapper;

    public GlobalExceptionHandler() {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
    }


    @ExceptionHandler(CmisUnauthorizedException.class)
    public ModelAndView handleCmisUnauthorizedException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {
        final UnauthorizedException unauthorizedException = new UnauthorizedException(request.getUserPrincipal().getName());

        return doResolveException(request, response, handler, unauthorizedException);
    }

    @ExceptionHandler(CmisPermissionDeniedException.class)
    public ModelAndView handleCmisPermissionDeniedException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {
        final PermissionDeniedException permissionDeniedException = new PermissionDeniedException(request.getUserPrincipal().getName());

        return doResolveException(request, response, handler, permissionDeniedException);
    }

    @ExceptionHandler(SearchRepositoryException.class)
    public ModelAndView handleSearchRepositoryException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, SearchRepositoryException ex) throws Exception {
        SearchRepositoryError repositoryError = mapper.readValue(ex.getErrorBody(), SearchRepositoryError.class);

        Exception searchRepositoryException = ex;
        final HttpStatus httpStatus = HttpStatus.resolve(repositoryError.getStatusCode());
        if (httpStatus != null) {
            switch (httpStatus) {
                case UNAUTHORIZED:
                    searchRepositoryException = new UnauthorizedException(request.getUserPrincipal().getName());
                    break;
                case FORBIDDEN:
                    searchRepositoryException = new PermissionDeniedException(request.getUserPrincipal().getName());
                    break;
            }
        }

        return doResolveException(request, response, handler, searchRepositoryException);
    }


}
