package edu.uw.edm.contentapi2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.controller.content.v1.model.PublicationResultResource;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.exceptions.ResourceNotFoundException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI;

/**
 * @Deprecated : Once work-api is completely deprecated this endpoint will need to be removed
 *
 * @author Maxime Deravet Date: 11/2/18
 */
@Service
@Slf4j
@Deprecated
public class PublicationService {

    private static final String PUBLISH_STATUS_FIELD_NAME = "PublishStatus";
    private static final String PUBLISH_STATUS_PUBLISHED = "Published";
    protected static final String CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME = "metadata.ClientProcessDefinitionKey";
    protected static final String CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME = "metadata.ClientProcessInstanceId";
    private DocumentFacade documentFacade;

    private RetryTemplate retryTemplate;

    @Autowired
    public PublicationService(DocumentFacade documentFacade, RetryTemplate retryTemplate) {
        this.documentFacade = documentFacade;
        this.retryTemplate = retryTemplate;
    }

    public PublicationResultResource updatePublication(Map<String, String> allRequestParams, String profile, String clientProcessDefinitionKey, String clientProcessInstanceId, User user) throws RepositoryException {
        SearchQueryModel searchQueryModel = new SearchQueryModel();
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_DEFINITION_KEY_FIELD_NAME, clientProcessDefinitionKey, false));
        searchQueryModel.addFilter(new SimpleSearchFilter(CLIENT_PROCESS_INSTANCE_ID_FIELD_NAME, clientProcessInstanceId, false));


        SearchResultContainer searchResultContainer = documentFacade.searchDocuments(profile, searchQueryModel, user);

        if (searchResultContainer.getTotalCount() == 0) {
            throw new ResourceNotFoundException("No items found for ClientProcessDefinitionKey '" + clientProcessDefinitionKey + "' and ClientProcessInstanceId '" + clientProcessInstanceId + "'");
        }

        List<ContentAPIDocument> updatedDocuments = new ArrayList<>();
        List<ContentAPIDocument> failedDocuments = new ArrayList<>();

        for (SearchResult searchResult : searchResultContainer.getSearchResults()) {
            ContentAPIDocument document = searchResult.getDocument();

            allRequestParams.entrySet().forEach(entry -> {
                if (ContentAPI.LABEL.equals(entry.getKey())) {
                    document.setLabel(entry.getValue());
                } else {
                    document.getMetadata().put(entry.getKey(), entry.getValue());
                }
            });

            document.getMetadata().put(PUBLISH_STATUS_FIELD_NAME, PUBLISH_STATUS_PUBLISHED);
            //Update publish status to Published

            try {
                ContentAPIDocument updatedDocument = retryTemplate.execute(retryContext -> {
                    if (retryContext.getRetryCount() > 0) {
                        log.info("retrying update. Last Error was " + retryContext.getLastThrowable().getMessage(), retryContext.getLastThrowable());
                    }

                    return documentFacade.updateDocument(document.getId(), document, user);
                });
                updatedDocuments.add(updatedDocument);
            } catch (RepositoryException e) {
                log.error("Couldn't update publication status of " + document.getId(), e);
                failedDocuments.add(document);
            }
        }


        PublicationResultResource publicationResultResource = new PublicationResultResource();
        publicationResultResource.setItemsAffected(updatedDocuments.stream().map(ContentAPIDocument::getId).collect(Collectors.toList()));


        return publicationResultResource;

    }
}
