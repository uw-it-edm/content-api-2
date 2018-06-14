package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.Constants.Alfresco.ExtensionNames.MANDATORY_ASPECTS;

@Service
@Slf4j
public class ACSProfileRepository {


    @Cacheable(value = "profiles", key = "#contentTypeId")
    public Map<String, PropertyDefinition<?>> getPropertyDefinition(Session session, String contentTypeId) {
        checkNotNull(session, "Session required.");
        checkNotNull(contentTypeId, "Content Type Required");

        log.trace("not hitting cache for content-type '{}' ", contentTypeId);

        final ObjectType typeDefinition = session.getTypeDefinition(contentTypeId, false);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition.getPropertyDefinitions(); //add content-type properties

        final List<String> mandatoryAspectIds = typeDefinition.getExtensions().stream()
                .filter(extension -> extension.getName().equals(MANDATORY_ASPECTS))
                .flatMap(extension -> extension.getChildren().stream())
                .map(cmisExtensionElement -> cmisExtensionElement.getValue())
                .collect(Collectors.toList());

        for (String aspectId : mandatoryAspectIds) {
            final Map<String, PropertyDefinition<?>> aspectProperties = session.getTypeDefinition(aspectId, false)
                    .getPropertyDefinitions();
            propertyDefinitions.putAll(aspectProperties); //add mandatory aspect properties
        }

        return propertyDefinitions;
    }
}
