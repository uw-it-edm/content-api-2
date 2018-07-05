package edu.uw.edm.contentapi2.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

public interface FileServingService {
    void serveFile(String itemId, ContentRenditionType renditionType, ContentDispositionType contentDispositionType, boolean useChannel, User user, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, IOException;
}
