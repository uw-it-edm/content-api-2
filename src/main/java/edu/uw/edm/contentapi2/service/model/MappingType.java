package edu.uw.edm.contentapi2.service.model;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;

public enum MappingType {

    integer, string, date, object, bool, referenced;

    //TODO: handle Mapping types: object, referenced
    public static MappingType fromPropertyType(PropertyType propertyType) {
        MappingType result;
        switch (propertyType) {
            case BOOLEAN:
                result = bool;
                break;
            case DATETIME:
                result = date;
                break;
            case ID:
                result = string;
                break;
            case INTEGER:
                result = integer;
                break;
            case STRING:
                result = string;
                break;
            default:
                result = string;
                break;
        }
        return result;
    }
}
