package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Preconditions;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.constants.Constants;
import edu.uw.edm.contentapi2.controller.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.FileServingService;
import edu.uw.edm.contentapi2.service.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileServingServiceImpl implements FileServingService {
    private ExternalDocumentRepository<Document> externalDocumentRepository;

    @Autowired
    public FileServingServiceImpl(ExternalDocumentRepository<Document> externalDocumentRepository) {
        this.externalDocumentRepository = externalDocumentRepository;
    }

    @Override
    public void serveFile(String itemId, ContentRenditionType renditionType, ContentDispositionType contentDispositionType, boolean useChannel, User user, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, IOException {
        Preconditions.checkArgument(!StringUtils.isEmpty(itemId), "Item Id is required.");
        Preconditions.checkNotNull(renditionType, "Rendition type is required.");
        Preconditions.checkNotNull(contentDispositionType, "Content Disposition Type is required.");
        Preconditions.checkNotNull(user, "User is required.");
        Preconditions.checkNotNull(request, "Request is required.");
        Preconditions.checkNotNull(response, "Response is required.");

        //TODO: handle http range requests

        final Document document = externalDocumentRepository.getDocumentById(itemId, user);

        final ContentStream contentStream = document.getContentStream();

        log.debug("Serving {}  with mime type {} and content length {}.", itemId, contentStream.getMimeType(), contentStream.getLength());
        response.setContentType(contentStream.getMimeType());


        final String fileName = generateServedFileName(document);
        addContentDispositionHeaderToResponse(contentDispositionType, response, fileName);

        if (useChannel) {
            StreamUtils.channelCopy(contentStream.getStream(), response.getOutputStream());
        } else {
            StreamUtils.streamCopy(contentStream.getStream(), response.getOutputStream());
        }
    }


    private String generateServedFileName(Document document) {
        final String originalFileName = getOriginalFileName(document);
        final int indexOfFileExtension = originalFileName.lastIndexOf(".");
        final String fileExtension = (indexOfFileExtension != -1) ? originalFileName.substring(indexOfFileExtension) : "";
        final String fileName = (indexOfFileExtension != -1) ? document.getId() + fileExtension : document.getId();
        return fileName;
    }

    private String getOriginalFileName(Document document) {
        //TODO: Is there a better way to handle this? is title ensured to be a property?
        return document.getProperties().stream().filter(p -> p.getLocalName().equals("title")).collect(Collectors.toList()).get(0).getValue();

    }


    private void addContentDispositionHeaderToResponse(ContentDispositionType contentDispositionType, HttpServletResponse response, String filename) {
        switch (contentDispositionType) {
            case none:
                //Do nothing, let the browser handle it
                break;
            case inline:
                response.setHeader(Constants.Headers.CONTENT_DISPOSITION, "inline;filename=\"" + filename + "\"");
                break;
            case attachment:
                response.setHeader(Constants.Headers.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"");
                break;
        }
    }
}
