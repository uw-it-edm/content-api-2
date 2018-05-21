package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Strings;

import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uw.edm.contentapi2.controller.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@Service
public class DocumentFacadeImpl implements DocumentFacade {

    private ExternalDocumentRepository<Document> repository;
    private ExternalDocumentConverter<Document> converter;

    @Autowired
    public DocumentFacadeImpl(ExternalDocumentRepository<Document> repository, ExternalDocumentConverter<Document> converter) {
        this.repository = repository;
        this.converter = converter;
    }

    @Override
    public ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");

        return converter.toContentApiDocument(repository.getDocumentById(documentId, user));
    }

    @Override
    public ContentAPIDocument createDocument(ContentAPIDocument contentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {

        checkNotNull(user, "User is required");
        checkNotNull(contentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(repository.createDocument(contentAPIDocument, primaryFile, user));
    }

    @Override
    public ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkNotNull(itemId, "ItemId is required");
        checkArgument(itemId.equals(updatedContentAPIDocument.getId()), "DocumentId doesn't match metadata");
        checkNotNull(updatedContentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(repository.updateDocument(itemId, updatedContentAPIDocument, primaryFile, user));
    }
}
