package edu.uw.edm.contentapi2.service.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class ProfileDefinitionV4 {
    private final String profile;

    private FieldDefinition id;
    private FieldDefinition label;

    private Map<String, FieldDefinition> metadata = new HashMap<>();
}
