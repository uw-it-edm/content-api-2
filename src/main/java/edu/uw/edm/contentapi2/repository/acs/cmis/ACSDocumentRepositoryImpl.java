package edu.uw.edm.contentapi2.repository.acs.cmis;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.ExternalProfileRepository;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.AlfrescoAspects;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.AlfrescoFields;
import edu.uw.edm.contentapi2.repository.exceptions.CannotUpdateDocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.DocumentAlreadyExistsException;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchDocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.NotLatestVersionOfDocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@Service
@Slf4j
public class ACSDocumentRepositoryImpl implements ExternalDocumentRepository<Document> {

    private ACSSessionCreator sessionCreator;
    private ACSProperties acsProperties;
    private ProfileFacade profileFacade;
    private ExternalProfileRepository profileRepository;
    private SiteFinder siteFinder;

    @Autowired
    public ACSDocumentRepositoryImpl(ACSSessionCreator sessionCreator, ACSProperties acsProperties, ExternalProfileRepository profileRepository, ProfileFacade profileFacade, SiteFinder siteFinder) {
        this.sessionCreator = sessionCreator;
        this.acsProperties = acsProperties;
        this.profileRepository = profileRepository;
        this.profileFacade = profileFacade;
        this.siteFinder = siteFinder;
    }

    @Override
    public void deleteDocumentById(String documentId, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");

        Session session = sessionCreator.getSessionForUser(user);

        Document documentById = getDocumentById(documentId, session);

        try {
            session.delete(documentById, true);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Get info about all the available renditions by default
     */
    @Override
    public Document getDocumentById(String documentId, User user) throws RepositoryException {
        return getDocumentById(documentId, user, RepositoryConstants.CMIS.Renditions.Filters.ALL);
    }

    @Override
    public Document getDocumentById(String documentId, User user, String renditionFilter) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");

        log.debug("getting document '{}' for user '{}' with renditionFilter '{}'", documentId, user.getUsername(), renditionFilter);
        Session sessionForUser = sessionCreator.getSessionForUser(user);

        return getDocumentById(documentId, sessionForUser, renditionFilter);
    }


    @Override
    public Document createDocument(ContentAPIDocument document, MultipartFile primaryFile, User user) throws NoSuchProfileException, DocumentAlreadyExistsException {
        checkNotNull(user, "User is required");
        checkNotNull(document, "Document is required");

        Session session = sessionCreator.getSessionForUser(user);

        Folder siteRootFolder = siteFinder.getSiteRootFolderFromContentApiDocument(document, user);

        ContentStream contentStream = getCMISContentStream(primaryFile, session);

        Map<String, Object> properties = getCMISProperties(document, primaryFile.getOriginalFilename(), session, user);
        try {
            return siteRootFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
        } catch (CmisContentAlreadyExistsException e) {
            log.error("Content With Same Name Already exists", e);
            throw new DocumentAlreadyExistsException(e.getMessage());
        } catch (Exception e) {
            log.error("couldn't create document", e);
            throw e;
        }
    }

    @Override
    public Document updateDocument(String documentId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws RepositoryException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");
        checkNotNull(updatedContentAPIDocument, "Document is required");

        Session session = sessionCreator.getSessionForUser(user);

        Document documentById = getDocumentById(documentId, session);

        if (primaryFile != null && !primaryFile.isEmpty()) {
            createNewRevisionWithFile(documentById, updatedContentAPIDocument, primaryFile, session, user);
            documentById = documentById.getObjectOfLatestVersion(true);
        } else {
            Map<String, Object> newProperties = getCMISPropertiesForUpdate(updatedContentAPIDocument, documentById.getName(), session, user);

            documentById.updateProperties(newProperties, true);
            //TODO, should I update minor revision ?
        }

        return documentById;
    }

    private Document getDocumentById(String documentId, Session sessionForUser) throws RepositoryException {
        return getDocumentById(documentId, sessionForUser, null);

    }

    private Document getDocumentById(String documentId, Session sessionForUser, String renditionFilter) throws RepositoryException {
        final OperationContext oc = sessionForUser.getDefaultContext();
        if (!Strings.isNullOrEmpty(renditionFilter)) {
            oc.setRenditionFilterString(renditionFilter);
        }

        final CmisObject cmisObject;
        try {
            cmisObject = sessionForUser.getObject(documentId, oc);
        } catch (CmisObjectNotFoundException nfe) {
            throw new NoSuchDocumentException(documentId);
        }

        if (cmisObject instanceof Document) {
            return (Document) cmisObject;
        } else {
            throw new NotADocumentException();
        }
    }

    private void createNewRevisionWithFile(Document documentToUpdate, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, Session session, User user) throws CannotUpdateDocumentException, NotLatestVersionOfDocumentException, DocumentAlreadyExistsException {

        if (!documentToUpdate.isLatestVersion()) {
            throw new NotLatestVersionOfDocumentException("Try again");
        }


        String checkedOutDocumentId;
        try {
            ObjectId oId = documentToUpdate.checkOut();
            checkedOutDocumentId = oId.getId();
            documentToUpdate.refresh();
        } catch (Exception e) {
            //TODO Document was probably already checked-out. Should we un-check it out ?
            if (acsProperties.isAutoUndoCheckout()) {
                documentToUpdate.cancelCheckOut();
                ObjectId oId = documentToUpdate.checkOut();
                checkedOutDocumentId = oId.getId();
            } else {
                throw new CannotUpdateDocumentException(e);
            }
        }
        Document newDocumentVersion = (Document) session.getObject(checkedOutDocumentId);
        try {
            Map<String, Object> cmisProperties = getCMISPropertiesForUpdate(updatedContentAPIDocument, primaryFile.getOriginalFilename(), session, user);

            // Forcing content stream file name to make sure cmis:name is updated
            cmisProperties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, getCMISName(primaryFile.getOriginalFilename(), updatedContentAPIDocument));

            addTypeAndAspectToProperties(cmisProperties, updatedContentAPIDocument, user);

            logProperties(cmisProperties);

            //TODO should we use major versions ?
            newDocumentVersion.checkIn(true, cmisProperties, getCMISContentStream(primaryFile, session), "");

        } catch (Exception e) {
            newDocumentVersion.cancelCheckOut();

            if (e instanceof CmisVersioningException && e.getMessage().contains("Cannot rename")) {
                log.info(e.getMessage());
                throw new DocumentAlreadyExistsException(e.getMessage() + " Please rename the provided file");
            }
            throw new CannotUpdateDocumentException(e);
        }
    }

    private void logProperties(Map<String, Object> cmisProperties) {
        if (log.isTraceEnabled()) {
            log.trace("sending update to cmis with these properties : ");
            log.trace("----------- ");
            cmisProperties.entrySet().forEach((entry) -> {
                log.trace(entry.getKey() + " -- " + entry.getValue());
            });
            log.trace("----------- ");
        }
    }

    private void addTypeAndAspectToProperties(Map<String, Object> cmisProperties, ContentAPIDocument updatedContentAPIDocument, User user) throws NoSuchProfileException {
        final String contentType = getFQDNContentType(updatedContentAPIDocument);
        cmisProperties.put(PropertyIds.OBJECT_TYPE_ID, contentType);

        final List<String> mandatoryAspectIds = profileRepository.getMandatoryAspects(user, contentType);
        cmisProperties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, mandatoryAspectIds);
    }

    private ContentStream getCMISContentStream(MultipartFile primaryFile, Session session) {
        ContentStream contentStream = null;
        if (primaryFile != null && !primaryFile.isEmpty()) {
            try {
                String mimeType = getMimeTypeFromFile(primaryFile);
                contentStream = session.getObjectFactory().createContentStream(primaryFile.getOriginalFilename(), primaryFile.getSize(), mimeType, primaryFile.getInputStream());
            } catch (IOException e) {
                //TODO use something else
                throw new RuntimeException(e);
            }
        } else {
            //NOOP, we create a document without file
        }
        return contentStream;
    }

    private String getMimeTypeFromFile(MultipartFile primaryFile) {
        return MimeTypes.getMIMEType(Iterables.getLast(Splitter.on('.').split(primaryFile.getOriginalFilename())));
    }

    private Map<String, Object> getCMISProperties(ContentAPIDocument document, String filename, Session session, User user) throws NoSuchProfileException {
        final Map<String, Object> properties = new HashMap<>();
        addTypeAndAspectToProperties(properties, document, user);
        properties.putAll(getCMISPropertiesForUpdate(document, filename, session, user));

        return properties;
    }

    private String getFQDNContentType(ContentAPIDocument document) throws NoSuchProfileException {
        checkNotNull(document, "document is required");
        checkNotNull(document.getMetadata(), "document metadata is required");
        final String profileId = document.getProfileId();
        final String contentType = profileFacade.getContentTypeForProfile(profileId);
        return contentType;
    }

    private Map<String, Object> getCMISPropertiesForUpdate(ContentAPIDocument document, String filename, Session session, User user) throws NoSuchProfileException {

        Map<String, Object> properties = new HashMap<>();
        properties.putAll(getFQDNPropertiesForMetadata(document, session, user));

        //Name is supposed to be unique in a folder ( and should be the file name )
        //TODO we should probably disable name uniqueness in ACS and remove the UUID
        //TODO on metadata update, we shouldn't update the name,
        properties.put(PropertyIds.NAME, getCMISName(filename, document));

        properties.put(AlfrescoFields.TITLE_FQDN, document.getLabel());

        //update shouldn't be allowed to change Profile
        properties.remove(PROFILE_ID);

        return properties;
    }

    private String getCMISName(String filename, ContentAPIDocument document) {
        String documentName;
        if (Strings.isNullOrEmpty(filename)) {
            documentName = document.getLabel();
        } else {
            documentName = filename;
        }
        return documentName;
    }

    private Map<String, Object> getFQDNPropertiesForMetadata(ContentAPIDocument document, Session session, User user) throws NoSuchProfileException {
        final Map<String, Object> properties = new HashMap<>();

        final String contentType = getFQDNContentType(document);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = profileRepository.getPropertyDefinition(user, contentType);
        propertyDefinitions.putAll(session.getTypeDefinition(AlfrescoAspects.TITLED).getPropertyDefinitions());

        final String profileId = document.getProfileId();

        for (Map.Entry<String, PropertyDefinition<?>> propertyDefinitionEntry : propertyDefinitions.entrySet()) {
            final String fqdnRepoFieldName = propertyDefinitionEntry.getKey();
            final String fieldLocalName = propertyDefinitionEntry.getValue().getLocalName();
            final String contentApiFieldName = profileFacade.convertToContentApiFieldFromRepositoryField(profileId, fieldLocalName);

            if (document.getMetadata().containsKey(contentApiFieldName)) { // Only add metadata passed in the document
                final Object contentApiMetaDataValue = document.getMetadata().get(contentApiFieldName);
                final Object repoMetadataValue = profileFacade.convertToRepoDataType(profileId, user, fqdnRepoFieldName, contentApiMetaDataValue);
                properties.put(fqdnRepoFieldName, repoMetadataValue);
            }
        }

        //TODO should we check if we missed a property ?

        return properties;
    }
}
