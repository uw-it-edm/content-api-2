package edu.uw.edm.contentapi2.controller.search.v1.model.result;

import java.util.LinkedList;
import java.util.List;

public class FacetResult {
    private String name;
    private List<BucketResult> buckets = new LinkedList<>();

    public FacetResult() {
    }

    public FacetResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BucketResult> getBuckets() {
        return buckets;
    }

    public void addBucketResult(BucketResult bucketResult) {
        this.buckets.add(bucketResult);
    }

    public void setBuckets(List<BucketResult> buckets) {
        this.buckets = buckets;
    }
}
