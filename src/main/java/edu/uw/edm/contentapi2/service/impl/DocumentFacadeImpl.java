package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Strings;

import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ProfiledSearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;
import edu.uw.edm.contentapi2.service.util.LegacySearchModelUtils;

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

        return converter.toContentApiDocument(contentRepository.getDocumentById(documentId, user), user);
    }

    @Override
    public ContentAPIDocument createDocument(ContentAPIDocument contentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {

        checkNotNull(user, "User is required");
        checkNotNull(contentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(contentRepository.createDocument(contentAPIDocument, primaryFile, user), user);
    }

    @Override
    public ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, User user) throws RepositoryException {
        return this.updateDocument(itemId, updatedContentAPIDocument, null, user);
    }

    @Override
    public ContentAPIDocument updateDocument(String itemId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkNotNull(itemId, "ItemId is required");
        checkArgument(itemId.equals(updatedContentAPIDocument.getId()), "DocumentId doesn't match metadata");
        checkNotNull(updatedContentAPIDocument, "Document Metadata is required");

        return converter.toContentApiDocument(contentRepository.updateDocument(itemId, updatedContentAPIDocument, primaryFile, user), user);
    }

    @Deprecated
    @Override
    public DocumentSearchResults searchDocuments(LegacySearchModel legacySearchModel, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkNotNull(legacySearchModel, "LegacySearchModel is required");

        final ProfiledSearchQueryModel searchQueryModel = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        final SearchResultContainer searchResults = searchDocuments(searchQueryModel.getProfileId(), searchQueryModel, user);

        return LegacySearchModelUtils.convertToDocumentSearchResults(searchResults);
    }

    @Override
    public SearchResultContainer searchDocuments(String profile, SearchQueryModel searchQueryModel, User user) throws RepositoryException {
        checkNotNull(profile, "Profile is required");
        checkNotNull(searchQueryModel, "Search is required");
        checkNotNull(user, "User is required");

        return searchRepository.searchDocuments(profile, searchQueryModel, user);
    }

}
