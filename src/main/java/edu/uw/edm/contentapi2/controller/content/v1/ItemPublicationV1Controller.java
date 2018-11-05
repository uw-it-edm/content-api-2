package edu.uw.edm.contentapi2.controller.content.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import edu.uw.edm.contentapi2.controller.content.v1.model.PublicationResultResource;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.PublicationService;

/**
 * @author Maxime Deravet Date: 11/2/18
 */
@RestController
@RequestMapping("/content/v1/publication")
public class ItemPublicationV1Controller {

    private PublicationService publicationService;

    @Autowired
    public ItemPublicationV1Controller(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Deprecated
    @RequestMapping(
            value = "{profile}/{clientProcessDefinitionKey},{clientProcessInstanceId}",
            method = RequestMethod.POST)
    public PublicationResultResource updatePublication(
            @RequestParam Map<String, String> allRequestParams,
            @PathVariable("profile") String profile,
            @PathVariable("clientProcessDefinitionKey") String clientProcessDefinitionKey,
            @PathVariable("clientProcessInstanceId") String clientProcessInstanceId,
            @AuthenticationPrincipal User user) throws RepositoryException {

        PublicationResultResource publicationResultResource = publicationService.updatePublication(allRequestParams, profile, clientProcessDefinitionKey, clientProcessInstanceId, user);

        return publicationResultResource;

    }
}
