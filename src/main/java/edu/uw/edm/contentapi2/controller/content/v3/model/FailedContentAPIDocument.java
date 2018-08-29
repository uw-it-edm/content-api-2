package edu.uw.edm.contentapi2.controller.content.v3.model;

import lombok.Getter;

@Getter
public class FailedContentAPIDocument extends ContentAPIDocument {
    private final String error;
    private final Class exception;

    public FailedContentAPIDocument(ContentAPIDocument document, Exception exception) {
        super(document.asMap());
        this.error = exception.getMessage();
        this.exception = exception.getClass();

    }


}
