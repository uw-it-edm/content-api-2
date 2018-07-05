package edu.uw.edm.contentapi2.repository;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 6/28/18
 */
public interface ExternalProfileRepository {
    @Cacheable(value = "profiles", key = "#contentTypeId")
    Map<String, PropertyDefinition<?>> getPropertyDefinition(User user, String contentTypeId);
}
