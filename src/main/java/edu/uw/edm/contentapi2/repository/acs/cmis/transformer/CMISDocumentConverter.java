package edu.uw.edm.contentapi2.repository.acs.cmis.transformer;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.transformer.ExternalDocumentConverter;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;

/**
 * @author Maxime Deravet Date: 4/3/18
 */

@Service
@Slf4j
public class CMISDocumentConverter implements ExternalDocumentConverter<org.apache.chemistry.opencmis.client.api.Document> {

    public static final String FILE_EXTENSION_SEPARATOR = ".";
    public static final String WEB_EXTENSION_PDF = "pdf";
    public static final String RENDITION_TYPE_PDF = "pdf";
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

        final Map<String, Object> contentApiMetadata = profileFacade.convertMetadataToContentApiDataTypes(cmisDocument, user, profile);
        contentAPIDocument.getMetadata().putAll(contentApiMetadata);
        contentAPIDocument.getMetadata().put(PROFILE_ID, profile);


        setWebExtensionInfo(cmisDocument, contentAPIDocument);

        return contentAPIDocument;
    }

    private void setWebExtensionInfo(Document cmisDocument, ContentAPIDocument contentAPIDocument) {
        String fileName = cmisDocument.getContentStreamFileName();
        if (fileName != null && fileName.contains(FILE_EXTENSION_SEPARATOR)) {
            contentAPIDocument.getMetadata().put(RepositoryConstants.ContentAPI.WEB_EXTENSION, fileName.substring(fileName.lastIndexOf(FILE_EXTENSION_SEPARATOR) + 1));
        }
        contentAPIDocument.getMetadata().put(RepositoryConstants.ContentAPI.FILE_SIZE, cmisDocument.getContentStreamLength());

        if (!CollectionUtils.isEmpty(cmisDocument.getRenditions())) {
            Optional<Rendition> firstPdfRendition = cmisDocument.getRenditions().stream().filter(rendition -> RENDITION_TYPE_PDF.equals(rendition.getKind())).findFirst();
            if (firstPdfRendition.isPresent()) {
                //Override webExtensions and FileSize
                log.debug("Found a pdf rendition");
                log.debug("Rendition ID: " + firstPdfRendition.get().getRenditionDocumentId());
                log.debug("stream ID: " + firstPdfRendition.get().getStreamId());
                contentAPIDocument.getMetadata().put(RepositoryConstants.ContentAPI.WEB_EXTENSION, WEB_EXTENSION_PDF);
                contentAPIDocument.getMetadata().put(RepositoryConstants.ContentAPI.FILE_SIZE, firstPdfRendition.get().getLength());
            }
        }
    }

    private String removeVersionFromId(String docId) {
        if (docId.contains(";")) {
            return docId.split(";")[0];
        } else {
            return docId;
        }
    }
}
