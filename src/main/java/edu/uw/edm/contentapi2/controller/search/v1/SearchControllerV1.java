package edu.uw.edm.contentapi2.controller.search.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

/**
 * @author Maxime Deravet Date: 6/21/18
 */
@RestController
@RequestMapping("/search/v1")
public class SearchControllerV1 {

    private DocumentFacade documentFacade;


    @Autowired
    public SearchControllerV1(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/{profile}")
    public SearchResultContainer searchInIndex(
            @PathVariable("profile") String profile,
            @RequestBody @Valid SearchQueryModel searchQueryModel,
            @AuthenticationPrincipal User user) throws RepositoryException {

        return documentFacade.searchDocuments(profile, searchQueryModel, user);
    }

}
