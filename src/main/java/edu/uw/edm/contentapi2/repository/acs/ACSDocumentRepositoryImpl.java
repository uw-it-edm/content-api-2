package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.acs.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet
 * Date: 4/3/18
 */
@Service
@Slf4j
public class ACSDocumentRepositoryImpl implements ExternalDocumentRepository<Document> {

    private ACSSessionCreator sessionCreator;

    @Autowired
    public ACSDocumentRepositoryImpl(ACSSessionCreator sessionCreator) {
        this.sessionCreator = sessionCreator;
    }


    @Override
    public Document getDocumentById(String documentId, User user) throws NotADocumentException {

        log.debug("getting document {} for user {}",documentId, user.getUsername());
        CmisObject cmisObject = sessionCreator.getSession().getObject(documentId);

        if (cmisObject instanceof Document) {
            return (Document) cmisObject;
        } else {
            throw new NotADocumentException();
        }


    }

}
