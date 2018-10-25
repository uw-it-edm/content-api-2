package edu.uw.edm.contentapi2.controller.content.v3;

import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentsUpdateResult;
import edu.uw.edm.contentapi2.controller.content.v3.model.FailedContentAPIDocument;
import edu.uw.edm.contentapi2.properties.ContentApiProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

@RestController
@RequestMapping("/content/v3/bulk")
public class BulkV3Controller {


    private final DocumentFacade documentFacade;
    private final ContentApiProperties contentApiProperties;

    @Autowired
    public BulkV3Controller(ContentApiProperties contentApiProperties, DocumentFacade documentFacade) {
        this.contentApiProperties = contentApiProperties;
        this.documentFacade = documentFacade;

    }

    @RequestMapping(method = RequestMethod.POST, path = "/item")
    public ResponseEntity<DocumentsUpdateResult> updateItems(
            @RequestBody List<ContentAPIDocument> documents,
            @AuthenticationPrincipal User user) {

        validateNumberOfDocumentsToUpdate(documents);
        DocumentsUpdateResult updateResult = new DocumentsUpdateResult();

        for(ContentAPIDocument document: documents){
            try {
                updateResult.addSuccess(documentFacade.updateDocument(document.getId(), document, user));
            } catch(Exception e){
                updateResult.addFailure(new FailedContentAPIDocument(document, e));
            }
        }


        return new ResponseEntity<>(updateResult, getResponseCodeForBulkItemUpdate(updateResult));
    }

    private void validateNumberOfDocumentsToUpdate(List<ContentAPIDocument> documents){
        Preconditions.checkArgument((documents == null) || (documents.size() > 0),"One or more documents required." );
        Preconditions.checkArgument( (documents.size() <= contentApiProperties.getBulkUpdateMaxItems()),"Only %s documents may be updated at a time. Received: %s",contentApiProperties.getBulkUpdateMaxItems(), documents.size() );

    }

    private HttpStatus getResponseCodeForBulkItemUpdate(DocumentsUpdateResult result) {
        if (result.getFailures().size() != 0) {
            if (result.getSuccesses().size() == 0) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            } else {
                return HttpStatus.ACCEPTED;
            }
        }

        return HttpStatus.OK;
    }

}
