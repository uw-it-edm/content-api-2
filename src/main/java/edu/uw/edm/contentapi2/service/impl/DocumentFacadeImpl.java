package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Strings;

import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
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

    private ExternalDocumentRepository<Document> contentRepository;
    private ExternalSearchDocumentRepository searchRepository;
    private ExternalDocumentConverter<Document> converter;

    @Autowired
    public DocumentFacadeImpl(ExternalDocumentRepository<Document> contentRepository, ExternalSearchDocumentRepository searchRepository, ExternalDocumentConverter<Document> converter) {
        this.contentRepository = contentRepository;
        this.searchRepository = searchRepository;
        this.converter = converter;
    }

    @Override
    public ContentAPIDocument getDocumentById(String documentId, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");

        return converter.toContentApiDocument(contentRepository.getDocumentById(documentId, user));
    }

    @Override
    public ContentAPIDocument createDocument(ContentAPIDocument contentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {

        checkNotNull(user, "User is required");
        checkNotNull(contentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(contentRepository.createDocument(contentAPIDocument, primaryFile, user));
    }

    @Override
    public ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkNotNull(itemId, "ItemId is required");
        checkArgument(itemId.equals(updatedContentAPIDocument.getId()), "DocumentId doesn't match metadata");
        checkNotNull(updatedContentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(contentRepository.updateDocument(itemId, updatedContentAPIDocument, primaryFile, user));
    }

    @Override
    public DocumentSearchResults searchDocuments(SearchModel searchModel, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkNotNull(searchModel, "SearchModel is required");

        //TODO searchDocuments should return total size
        Set<ContentAPIDocument> documents = contentRepository.searchDocuments(searchModel, user)
                .stream()
                .map(converter::toContentApiDocument)
                .collect(Collectors.toSet());


        return DocumentSearchResults
                .builder()
                .documents(documents)
                .totalCount(documents.size())
                .build();
    }

    @Override
    public SearchResultContainer searchDocuments(String profile, SearchQueryModel searchQueryModel, User user) throws RepositoryException {
        checkNotNull(profile, "Profile is required");
        checkNotNull(searchQueryModel, "Search is required");
        checkNotNull(user, "User is required");

        return searchRepository.searchDocuments(profile, searchQueryModel, user);
    }

}
