package edu.uw.edm.contentapi2.service.model;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.repository.constants.Constants;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProfileDefinitionV4 {
    private final String profile;

    private FieldDefinition id;
    private FieldDefinition label;

    private Map<String, FieldDefinition> metadata = new HashMap<>();

    public ProfileDefinitionV4(String profile, Map<String, PropertyDefinition<?>> acsMetadata) {
        this.profile = profile;
        acsMetadata.forEach((propertyName, propertyDefinition) -> {
            FieldDefinition fieldDefinition = FieldDefinition.builder()
                    .repoFieldName(propertyDefinition.getId())
                    .type(MappingType.fromPropertyType(propertyDefinition.getPropertyType()))
                    .validator("none")//TODO: implement validators
                    .build();

            if(propertyDefinition.getId().equals(Constants.Alfresco.AlfrescoFields.ITEM_ID_FQDN)){
                this.id = fieldDefinition;
            }else if (propertyDefinition.getId().equals(Constants.Alfresco.AlfrescoFields.LABEL_FQDN)){
                this.label = fieldDefinition;
            }else{
                this.metadata.put(propertyDefinition.getLocalName(),fieldDefinition);
            }
        });
    }
}
