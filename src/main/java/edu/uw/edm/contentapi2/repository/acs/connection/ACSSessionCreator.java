package edu.uw.edm.contentapi2.repository.acs.connection;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 4/4/18
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
    public Session getSessionForUser(User user) {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();

        SessionParameterMap parameters = new SessionParameterMap();
        //TODO
// user credentials
        parameters.setUserAndPassword(acsProperties.getUser(), acsProperties.getPassword());
        parameters.addHeader(acsProperties.getActAsHeader(), user.getUsername());

// connection settings
        parameters.setAtomPubBindingUrl(acsProperties.getCmisUrl());

// create session
        List<Repository> repositories = factory.getRepositories(parameters);
        return repositories.get(0).createSession();
    }
}
