package edu.uw.edm.contentapi2.controller.content.v3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

/**
 * @author Maxime Deravet Date: 3/27/18
 */
@RestController
@RequestMapping("/content/v3/item")
public class ItemV3Controller {


    private DocumentFacade documentFacade;

    @Autowired
    public ItemV3Controller(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }


    /**
     * @deprecated Replaced by {@link edu.uw.edm.contentapi2.controller.search.v1.SearchController#searchInIndex(String
     * profile, SearchQueryModel searchQueryModel, User user)}
     */
    @Deprecated
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public DocumentSearchResults _searchItems(
            @RequestBody @Valid LegacySearchModel legacySearchModel,
            @AuthenticationPrincipal User user) throws RepositoryException {

        final DocumentSearchResults documentSearchResults = documentFacade.searchDocuments(legacySearchModel, user);

        return documentSearchResults;
    }


    @RequestMapping("/{itemId}")
    public ContentAPIDocument getItem(@PathVariable("itemId") String itemId, @AuthenticationPrincipal User user) throws RepositoryException {

        return documentFacade.getDocumentById(itemId, user);
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ContentAPIDocument createItem(
            @RequestPart(value = "document", required = true) @Valid ContentAPIDocument contentAPIDocument,
            @RequestPart(value = "attachment", required = false) MultipartFile primaryFile,
            @AuthenticationPrincipal User user) throws RepositoryException {

        return documentFacade.createDocument(contentAPIDocument, primaryFile, user);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{itemId}")
    public ContentAPIDocument updateItem(
            @PathVariable("itemId") String itemId,
            @RequestPart(value = "document", required = true) @Valid ContentAPIDocument updatedContentAPIDocument,
            @RequestPart(value = "attachment", required = false) MultipartFile primaryFile,
            @AuthenticationPrincipal User user) throws RepositoryException {

        return documentFacade.updateDocument(itemId, updatedContentAPIDocument, primaryFile, user);
    }


}

