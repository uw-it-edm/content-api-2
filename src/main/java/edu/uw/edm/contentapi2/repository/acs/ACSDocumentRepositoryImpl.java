package edu.uw.edm.contentapi2.repository.acs;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.acs.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
        checkNotNull(user,"User is required");
        checkArgument(Strings.isNotEmpty(documentId), "DocumentId is required");

        log.debug("getting document {} for user {}",documentId, user.getUsername());
        CmisObject cmisObject = sessionCreator.getSessionForUser(user).getObject(documentId);

        if (cmisObject instanceof Document) {
            return (Document) cmisObject;
        } else {
            throw new NotADocumentException();
        }

    }

}
