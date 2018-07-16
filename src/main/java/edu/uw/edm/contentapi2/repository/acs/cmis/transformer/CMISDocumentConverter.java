package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.exceptions.UndefinedFieldException;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@Slf4j
@Service
public class CMISDocumentConverter implements ExternalDocumentConverter<org.apache.chemistry.opencmis.client.api.Document> {
    private static final String ALFRESCO_SYSTEM_PREFIX = "sys:";
    private ProfileFacade profileFacade;

    @Autowired
    public CMISDocumentConverter(ProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }

    @Override
    public ContentAPIDocument toContentApiDocument(Document cmisDocument, User user) throws NoSuchProfileException {
        checkNotNull(cmisDocument, "cmisDocument is required");
        checkNotNull(cmisDocument.getId(), "cmisDocument id is required");
        checkNotNull(cmisDocument.getDocumentType(), "cmisDocument type is required");
        final String profile = cmisDocument.getDocumentType().getLocalName();

        final ContentAPIDocument contentAPIDocument = new ContentAPIDocument();
        contentAPIDocument.setId(removeVersionFromId(cmisDocument.getId()));

        contentAPIDocument.setLabel(cmisDocument.getPropertyValue(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN));

        for (Property property : cmisDocument.getProperties()) {
            if (!property.getId().startsWith(ALFRESCO_SYSTEM_PREFIX)) { // do not share system properties
                final String fieldName = profileFacade.convertToContentApiFieldFromRepositoryField(profile, property.getLocalName());
                try {
                    final Object fieldValue = profileFacade.convertToContentApiDataType(profile, user, property.getId(), property.getValue());
                    contentAPIDocument.getMetadata().put(fieldName, fieldValue);
                } catch (UndefinedFieldException undefinedFieldException) {
                    log.trace(undefinedFieldException.getMessage());
                }

            }
        }

        contentAPIDocument.getMetadata().put(PROFILE_ID, profile);

        return contentAPIDocument;
    }

    private String removeVersionFromId(String docId) {
        if (docId.contains(";")) {
            return docId.split(";")[0];
        } else {
            return docId;
        }
    }
}
