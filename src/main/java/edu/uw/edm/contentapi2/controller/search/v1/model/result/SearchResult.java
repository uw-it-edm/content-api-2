package edu.uw.edm.contentapi2.controller.search.v1.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;

public class SearchResult extends ContentAPIDocument {

    private float _score;

    @Deprecated
    private String _indexName;

    public String get_indexName() {
        return _indexName;
    }

    public void setIndexName(String _indexName) {
        this._indexName = _indexName;
    }

    public float get_score() {
        return _score;
    }

    public void setScore(float score) {
        this._score = score;
    }

    public void setDocument(ContentAPIDocument document) {
        this.setId(document.getId());
        this.setLabel(document.getLabel());
        this.setMetadata(document.getMetadata());
    }

    @JsonIgnore
    public ContentAPIDocument getDocument() {

        ContentAPIDocument document = new ContentAPIDocument();
        document.setId(this.getId());
        document.setLabel(this.getLabel());
        document.setMetadata(this.getMetadata());

        return document;
    }

}
