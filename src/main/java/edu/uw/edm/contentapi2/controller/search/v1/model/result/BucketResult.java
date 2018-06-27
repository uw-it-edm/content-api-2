package edu.uw.edm.contentapi2.controller.search.v1.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketResult {
    private long count;
    private String key;


}
