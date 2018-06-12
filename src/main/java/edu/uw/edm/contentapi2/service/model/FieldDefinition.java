package edu.uw.edm.contentapi2.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FieldDefinition {
    private MappingType type;
    private String wccFieldName;
    //private String acsFieldName;
    private String validator;
}
