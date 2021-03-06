package edu.uw.edm.contentapi2.repository.transformer;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface ExternalDocumentConverter<Document> {
    ContentAPIDocument toContentApiDocument(Document cmisDocument, User user) throws NoSuchProfileException;
}
