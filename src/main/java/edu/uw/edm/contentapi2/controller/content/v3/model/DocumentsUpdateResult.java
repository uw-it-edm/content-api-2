package edu.uw.edm.contentapi2.controller.content.v3.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DocumentsUpdateResult {
    private final List<ContentAPIDocument> successes = new ArrayList<>();
    private final List<FailedContentAPIDocument> failures = new ArrayList<>();

    public void addSuccess(ContentAPIDocument document){
        this.successes.add(document);
    }

    public void addFailure(FailedContentAPIDocument document){
        this.failures.add(document);
    }
}
