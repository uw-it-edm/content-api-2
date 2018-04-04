package edu.uw.edm.contentapi2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
@Service
public class DocumentFacadeImpl implements DocumentFacade {

    private ExternalDocumentRepository repository;
    private ExternalDocumentConverter converter;

    @Autowired
    public DocumentFacadeImpl(ExternalDocumentRepository repository, ExternalDocumentConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    @Override
    public ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException {

        return converter.toContentApiDocument(repository.getDocumentById(documentId, user));
    }
}
