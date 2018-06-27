package edu.uw.edm.contentapi2.repository.acs.cmis;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchModel;
import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.acs.cmis.connection.ACSSessionCreator;
import edu.uw.edm.contentapi2.repository.constants.Constants.Alfresco.AlfrescoAspects;
import edu.uw.edm.contentapi2.repository.constants.Constants.Alfresco.AlfrescoFields;
import edu.uw.edm.contentapi2.repository.exceptions.CannotUpdateDocumentException;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.exceptions.NotADocumentException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static edu.uw.edm.contentapi2.repository.constants.Constants.ContentAPI.PROFILE_ID;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
@Service
@Slf4j
public class ACSDocumentRepositoryImpl implements ExternalDocumentRepository<Document> {

    private ACSSessionCreator sessionCreator;
    private ACSProperties acsProperties;
    private ACSProfileRepository profileRepository;
    private FieldMapper fieldMapper;

    @Autowired
    public ACSDocumentRepositoryImpl(ACSSessionCreator sessionCreator, ACSProperties acsProperties, ACSProfileRepository profileRepository, FieldMapper fieldMapper) {
        this.sessionCreator = sessionCreator;
        this.acsProperties = acsProperties;
        this.profileRepository = profileRepository;
        this.fieldMapper = fieldMapper;
    }


    @Override
    public Document getDocumentById(String documentId, User user) throws NotADocumentException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");


        log.debug("getting document {} for user {}", documentId, user.getUsername());
        Session sessionForUser = sessionCreator.getSessionForUser(user);

        return getDocumentById(documentId, sessionForUser);
    }

    @Override
    public Document createDocument(ContentAPIDocument document, MultipartFile primaryFile, User user) throws NoSuchProfileException {
        checkNotNull(user, "User is required");
        checkNotNull(document, "Document is required");

        Session session = sessionCreator.getSessionForUser(user);

        Folder siteRootFolder = getSiteRootFolderFromContentApiDocument(document, session);

        ContentStream contentStream = getCMISContentStream(primaryFile, session);

        Map<String, Object> properties = getCMISProperties(document, primaryFile.getOriginalFilename(), session);
        return siteRootFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
    }

    @Override
    public Document updateDocument(String documentId, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, User user) throws NotADocumentException, CannotUpdateDocumentException, NoSuchProfileException {
        checkNotNull(user, "User is required");
        checkArgument(!Strings.isNullOrEmpty(documentId), "DocumentId is required");
        checkNotNull(updatedContentAPIDocument, "Document is required");

        Session session = sessionCreator.getSessionForUser(user);

        Document documentById = getDocumentById(documentId, session);

        if (primaryFile != null && !primaryFile.isEmpty()) {
            createNewRevisionWithFile(documentById, updatedContentAPIDocument, primaryFile, session);
            documentById = documentById.getObjectOfLatestVersion(true);
        } else {
            Map<String, Object> newProperties = getCMISPropertiesForUpdate(updatedContentAPIDocument, documentById.getName(), session);

            documentById.updateProperties(newProperties, true);
            //TODO, should I update minor revision ?
        }

        return documentById;
    }

    private Document getDocumentById(String documentId, Session sessionForUser) throws NotADocumentException {
        CmisObject cmisObject = sessionForUser.getObject(documentId);

        if (cmisObject instanceof Document) {
            return (Document) cmisObject;
        } else {
            throw new NotADocumentException();
        }
    }

    private void createNewRevisionWithFile(Document documentToUpdate, ContentAPIDocument updatedContentAPIDocument, MultipartFile primaryFile, Session session) throws CannotUpdateDocumentException {
        ObjectId checkedOutDocumentId;
        try {
            checkedOutDocumentId = documentToUpdate.checkOut();
        } catch (Exception e) {
            //TODO Document was probably already checked-out. Should we un-check it out ?
            if (acsProperties.isAutoUndoCheckout()) {
                documentToUpdate.cancelCheckOut();
                checkedOutDocumentId = documentToUpdate.checkOut();
            } else {
                throw new CannotUpdateDocumentException(e);
            }
        }
        Document newDocumentVersion = (Document) session.getObject(checkedOutDocumentId);
        try {
            Map<String, Object> cmisProperties = getCMISPropertiesForUpdate(updatedContentAPIDocument, documentToUpdate.getName(), session);

            //TODO should we use major versions ?
            newDocumentVersion.checkIn(true, cmisProperties, getCMISContentStream(primaryFile, session), "");

        } catch (Exception e) {
            newDocumentVersion.cancelCheckOut();
            throw new CannotUpdateDocumentException(e);
        }
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

    private Map<String, Object> getCMISProperties(ContentAPIDocument document, String filename, Session session) throws NoSuchProfileException {
        final Map<String, Object> properties = new HashMap<>();
        final String contentType = getFQDNContentType(document);
        properties.put(PropertyIds.OBJECT_TYPE_ID, contentType);

        //This is where aspects need to be listed
        //TODO we'll need to check if we need to manually add the aspects or if ACS rules on the main folder can help
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(AlfrescoAspects.TITLED));
        properties.putAll(getCMISPropertiesForUpdate(document, filename, session));

        return properties;
    }

    private String getFQDNContentType(ContentAPIDocument document) throws NoSuchProfileException {
        checkNotNull(document, "document is required");
        checkNotNull(document.getMetadata(), "document metadata is required");
        final String profileId = document.getProfileId();
        final String contentType = fieldMapper.getContentTypeForProfile(profileId);
        return contentType;
    }

    private Map<String, Object> getCMISPropertiesForUpdate(ContentAPIDocument document, String filename, Session session) throws NoSuchProfileException {

        Map<String, Object> properties = new HashMap<>();
        //Name is supposed to be unique in a folder ( and should be the file name )
        //TODO we should probably disable name uniqueness in ACS and remove the UUID
        //TODO on metadata update, we shouldn't update the name,
        properties.put(PropertyIds.NAME, getCMISName(filename, document));

        properties.put(AlfrescoFields.TITLE_FQDN, document.getLabel());

        properties.putAll(getFQDNPropertiesForMetadata(document, session));

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
        return documentName + " " + UUID.randomUUID().toString();
    }

    private Map<String, Object> getFQDNPropertiesForMetadata(ContentAPIDocument document, Session session) throws NoSuchProfileException {
        final Map<String, Object> properties = new HashMap<>();

        final String contentType = getFQDNContentType(document);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = profileRepository.getPropertyDefinition(session, contentType);
        propertyDefinitions.putAll(session.getTypeDefinition(AlfrescoAspects.TITLED).getPropertyDefinitions());

        final String profileId = document.getProfileId();

        propertyDefinitions.forEach((key, propertyDefinition) -> {
            Object contentAPIMetadataValue = document.getMetadata().get(fieldMapper.convertToContentApiFieldFromRepositoryField(profileId, propertyDefinition.getLocalName()));

            if (contentAPIMetadataValue != null) {
                properties.put(key, contentAPIMetadataValue);
            }
        });

        //TODO should we check if we missed a property ?

        return properties;
    }


    /**
     * We get the root folder from the ContentAPI#PROFILE_ID metadata field
     */
    private Folder getSiteRootFolderFromContentApiDocument(ContentAPIDocument document, Session session) throws NoSuchProfileException {
        checkNotNull(document, "document metadata shouldn't be null");
        checkNotNull(document.getMetadata(), "document should contain metadata");
        String profileId = (String) document.getMetadata().get(PROFILE_ID);


        return getSiteRootFolderForProfileId(session, profileId);

    }

    private Folder getSiteRootFolderForProfileId(Session session, String profileId) throws NoSuchProfileException {
        checkArgument(!Strings.isNullOrEmpty(profileId), "Should have a profileId field");

        CmisObject documentLibraryFolderForProfile = session.getObjectByPath(getDocumentLibraryPath(profileId));

        if (documentLibraryFolderForProfile.getType() instanceof FolderType) {
            log.debug("using folder " + documentLibraryFolderForProfile.getName() + " at path " + ((Folder) documentLibraryFolderForProfile).getPath());
            return (Folder) documentLibraryFolderForProfile;
        } else {
            throw new NoSuchProfileException(profileId);
        }
    }

    private String getDocumentLibraryPath(String profileId) {
        return String.format("/Sites/%s/documentLibrary", profileId);
    }


    @Override
    public Map<String, PropertyDefinition<?>> getPropertyDefinition(User user, String contentType) {
        checkNotNull(user, "User required.");
        checkNotNull(contentType, "Content Type Required");

        final Session sessionForUser = sessionCreator.getSessionForUser(user);
        final Map<String, PropertyDefinition<?>> propertyDefinitions = profileRepository.getPropertyDefinition(sessionForUser, contentType);

        return propertyDefinitions;
    }

    @Override
    public Set<Document> searchDocuments(SearchModel searchModel, User user) throws NoSuchProfileException {

        final Session sessionForUser = sessionCreator.getSessionForUser(user);
        //OperationContext oc = sessionForUser.createOperationContext();
        //oc.setMaxItemsPerPage(searchModel.getPageSize());
        OperationContext oc = new OperationContextImpl(null, false, true, false,
                IncludeRelationships.NONE, null, true, null, true, 100);

        //oc.setLoadSecondaryTypeProperties(true);
        // QueryStatement qs = sessionForUser.createQueryStatement("select * from uwfi:FinancialAid where IN_TREE(?) and uwfi:financialAidYear IS NOT NULL");
        //QueryStatement qs = sessionForUser.createQueryStatement("select * from cmis:document");
        HashMap<String, String> kvHashMap = new HashMap<>();
        //kvHashMap.put("d", "cmis:document");
        kvHashMap.put("financialAid", "D:uwfi:FinancialAid");
        kvHashMap.put("uwstudent", "P:uw:student");

        sessionForUser.queryObjects("D:uwfi:FinancialAid", "cmis:objectId ='3d919f5f-43cb-46da-a9bf-b610a4b9c107'", false, oc).iterator().forEachRemaining(cmisObject -> {

                try {
                    Document documentById = getDocumentById(cmisObject.getId(), sessionForUser);
                    log.trace(documentById.getId());

                } catch (NotADocumentException e) {
                    e.printStackTrace();
                }
                log.trace(cmisObject.getId());
        });

        QueryStatement qs = sessionForUser.createQueryStatement(Arrays.asList("*"), kvHashMap, "", Arrays.asList());
        qs.setId(1, getSiteRootFolderForProfileId(sessionForUser, "FinancialAid"));

        ItemIterable<QueryResult> queryResults = qs.query(false, oc);

        //"select financialAid.*, uwstudent.* from P:uw:student uwstudent,D:uwfi:FinancialAid financialAid"

        queryResults.skipTo(searchModel.getPageSize() * searchModel.getPageStart())
                .getPage(searchModel.getPageSize()).iterator().forEachRemaining(queryResult -> {
                    queryResult.getProperties().toString();
                }
        );

        return null;
    }

}
