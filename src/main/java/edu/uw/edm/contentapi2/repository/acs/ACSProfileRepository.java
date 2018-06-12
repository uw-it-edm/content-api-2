package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
@Slf4j
public class ACSProfileRepository {

    @Cacheable(value = "profiles", key = "#contentType")
    public Map<String, PropertyDefinition<?>> getPropertyDefinition(Session session, String contentType) {
        checkNotNull(session, "Session required.");
        checkNotNull(contentType, "Content Type Required");

        log.trace("not hitting cache for content-type '{}' ", contentType);

        final Map<String, PropertyDefinition<?>> propertyDefinitions = session.getTypeDefinition(contentType, false).getPropertyDefinitions();

        return propertyDefinitions;
    }
}
