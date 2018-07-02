package edu.uw.edm.contentapi2.repository.transformer;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
public interface ExternalDocumentConverter<T> {
    ContentAPIDocument toContentApiDocument(T repositoryDocument);
}
