package edu.uw.edm.contentapi2.repository.acs.transformer;

import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
@Service
public class CMISDocumentConverter implements ExternalDocumentConverter<org.apache.chemistry.opencmis.client.api.Document> {


    @Override
    public ContentAPIDocument toContentApiDocument(org.apache.chemistry.opencmis.client.api.Document repositoryDocument) {
        checkNotNull(repositoryDocument, "repositoryDocument is required");

        ContentAPIDocument contentAPIDocument = new ContentAPIDocument();
        contentAPIDocument.setId(repositoryDocument.getId());
        contentAPIDocument.setLabel(repositoryDocument.getName());

        repositoryDocument.getProperties().forEach((property -> {
            contentAPIDocument.getMetadata().put(property.getLocalName(), property.getValue());
        }));

        return contentAPIDocument;
    }
}
