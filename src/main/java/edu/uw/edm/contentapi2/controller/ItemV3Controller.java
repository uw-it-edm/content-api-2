package edu.uw.edm.contentapi2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uw.edm.contentapi2.controller.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

/**
 * @author Maxime Deravet
 * Date: 3/27/18
 */
@RestController
@RequestMapping("/v3/item")
public class ItemV3Controller {


    private DocumentFacade documentFacade;

    @Autowired
    public ItemV3Controller(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }

    @RequestMapping("/{itemId}")
    public ContentAPIDocument getItem(@PathVariable("itemId") String itemId, @AuthenticationPrincipal User user) throws RepositoryException {

        return documentFacade.getDocumentById(itemId, user);
    }
}
