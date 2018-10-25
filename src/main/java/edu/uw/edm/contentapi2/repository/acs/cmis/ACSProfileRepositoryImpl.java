package edu.uw.edm.contentapi2.repository.acs.cmis;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.repository.ExternalProfileRepository;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.ExtensionNames.MANDATORY_ASPECTS;

@Service
@Slf4j
public class ACSProfileRepositoryImpl implements ExternalProfileRepository {

    private ACSSessionCreator sessionCreator;

    @Autowired
    public ACSProfileRepositoryImpl(ACSSessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }
    @Override
    @Cacheable(value = "profile-mandatory-aspects", key = "#contentTypeId")
    public List<String> getMandatoryAspects(User user, String contentTypeId){
        checkNotNull(user, "User required.");
        checkNotNull(contentTypeId, "Content Type Required");

        log.trace("not hitting  mandatoryAspects cache for content-type '{}' ", contentTypeId);

        final Session session = sessionCreator.getSessionForUser(user);
        final ObjectType typeDefinition = session.getTypeDefinition(contentTypeId, false);
        return extractMandatoryAspects(typeDefinition);

    }

    private List<String> extractMandatoryAspects(ObjectType typeDefinition){
        final List<String> mandatoryAspectIds = typeDefinition.getExtensions().stream()
                .filter(extension -> extension.getName().equals(MANDATORY_ASPECTS))
                .flatMap(extension -> extension.getChildren().stream())
                .map(cmisExtensionElement -> cmisExtensionElement.getValue())
                .collect(Collectors.toList());
        return mandatoryAspectIds;
    }

    @Override
    @Cacheable(value = "profiles", key = "#contentTypeId")
    public Map<String, PropertyDefinition<?>> getPropertyDefinition(User user, String contentTypeId) {
        checkNotNull(user, "User required.");
        checkNotNull(contentTypeId, "Content Type Required");

        log.trace("not hitting propertyDefinition cache for content-type '{}' ", contentTypeId);


        final Session session = sessionCreator.getSessionForUser(user);
        final ObjectType typeDefinition = session.getTypeDefinition(contentTypeId, false);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition.getPropertyDefinitions(); //add content-type properties

        final List<String> mandatoryAspectIds = extractMandatoryAspects(typeDefinition);

        for (String aspectId : mandatoryAspectIds) {
            final Map<String, PropertyDefinition<?>> aspectProperties = session.getTypeDefinition(aspectId, false)
                    .getPropertyDefinitions();
            propertyDefinitions.putAll(aspectProperties); //add mandatory aspect properties
        }

        return propertyDefinitions;
    }
}
