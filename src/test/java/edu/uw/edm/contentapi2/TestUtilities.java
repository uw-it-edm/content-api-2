package edu.uw.edm.contentapi2;

import java.util.HashMap;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;

public class TestUtilities {
    public static ContentAPIDocument getTestDocument() {
        return getTestDocument("123");
    }

    public static  ContentAPIDocument getTestDocument(String documentId) {
        ContentAPIDocument document123 = new ContentAPIDocument();

        document123.setId(documentId);
        document123.setLabel("document" + documentId + " title");

        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("ProfileId", "The Profile of the document");
        metadata.put("Account", "The Account of the document");
        metadata.put("OriginalFileName", "SamplePDF3217335768660836293.pdf");
        metadata.put("WebExtension", "pdf");
        metadata.put("Other", "any other metadata available in your documents");

        document123.setMetadata(metadata);
        return document123;
    }
}
