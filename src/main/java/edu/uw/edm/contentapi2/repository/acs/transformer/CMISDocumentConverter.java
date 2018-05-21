package edu.uw.edm.contentapi2.repository.acs.transformer;

import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.Constants;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
@Service
public class CMISDocumentConverter implements ExternalDocumentConverter<org.apache.chemistry.opencmis.client.api.Document> {


    @Override
    public ContentAPIDocument toContentApiDocument(Document cmisDocument) {
        checkNotNull(cmisDocument, "cmisDocument is required");

        ContentAPIDocument contentAPIDocument = new ContentAPIDocument();
        contentAPIDocument.setId(cmisDocument.getId());
        contentAPIDocument.setLabel(cmisDocument.getPropertyValue(Constants.Alfresco.AlfrescoFields.TITLE_FQDN));

        cmisDocument.getProperties().forEach((property -> {
            contentAPIDocument.getMetadata().put(property.getLocalName(), property.getValue());
        }));

        return contentAPIDocument;
    }
}
