package edu.uw.edm.contentapi2.repository.acs.connection;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.edm.contentapi2.properties.ACSProperties;

/**
 * @author Maxime Deravet
 * Date: 4/4/18
 */
@Service
public class ACSSessionCreator {

    private ACSProperties acsProperties;

    @Autowired
    public ACSSessionCreator(ACSProperties acsProperties) {
        this.acsProperties = acsProperties;
    }

    //TODO this need to handle user authentication, not just admin connection
    // it should also probably have some kind of cache/singleton
    public Session getSession() {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<>();

// user credentials
        parameters.put(SessionParameter.USER, acsProperties.getUser());
        parameters.put(SessionParameter.PASSWORD, acsProperties.getPassword());

// connection settings
        parameters.put(SessionParameter.ATOMPUB_URL, acsProperties.getCmisUrl());
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

// create session
        List<Repository> repositories = factory.getRepositories(parameters);
        return repositories.get(0).createSession();
    }
}
