package edu.uw.edm.contentapi2.service.impl;

import com.google.common.base.Preconditions;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.constants.ControllerConstants;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.FileServingService;
import edu.uw.edm.contentapi2.service.util.HttpRequestUtils;
import edu.uw.edm.contentapi2.service.util.PdfUtils;
import edu.uw.edm.contentapi2.service.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.CMIS.Renditions.THUMBNAIL_KIND;

@Slf4j
@Service
public class FileServingServiceImpl implements FileServingService {
    private Pattern WEB_VIEWABLE_MIME_TYPE_REGEX_PATTERN = Pattern.compile("application/pdf|image/png|image/gif|image/jpeg");
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


        final Document document = getDocumentWithRendition(itemId, renditionType, user);
        final ContentStream contentStream = getContentStreamWithRendition(document, renditionType);


        final String originalFileName = document.getContentStreamFileName();
        final String fileName = generateServedFileName(originalFileName, itemId);
        addContentDispositionHeaderToResponse(contentDispositionType, response, fileName);

        if (contentStream == null) {
            log.debug("Serving LinkToNativePdf for {}  ", itemId);
            serveLinkToNativePdf(request, response, itemId, originalFileName);

        } else {
            log.debug("Serving {}  with mime type {} and content length {}.", itemId, contentStream.getMimeType(), contentStream.getLength());
            response.setContentType(contentStream.getMimeType());

            if (useChannel) {
                StreamUtils.channelCopy(contentStream.getStream(), response.getOutputStream());
            } else {
                StreamUtils.streamCopy(contentStream.getStream(), response.getOutputStream());
            }
        }
    }

    private Document getDocumentWithRendition(String itemId, ContentRenditionType renditionType, User user) throws RepositoryException {
        String renditionFilter = RepositoryConstants.CMIS.Renditions.Filters.NONE;
        if (ContentRenditionType.Web.equals(renditionType)) {
            renditionFilter = RepositoryConstants.CMIS.Renditions.Filters.WEB;
        }
        return externalDocumentRepository.getDocumentById(itemId, user, renditionFilter);
    }

    private ContentStream getContentStreamWithRendition(Document document, ContentRenditionType renditionType) {
        ContentStream contentStream = null;
        if (ContentRenditionType.Primary.equals(renditionType)) {
            contentStream = document.getContentStream();
        } else if (ContentRenditionType.Web.equals(renditionType)) {
            final Matcher webViewableMimeType = WEB_VIEWABLE_MIME_TYPE_REGEX_PATTERN.matcher(document.getContentStreamMimeType());
            if (webViewableMimeType.matches()) { // if original document is webviewable return that
                contentStream = document.getContentStream();
            } else {
                contentStream = getWebRenditionContentStream(document);
            }

        } else {
            log.error("Unexpected rendition type: '{}', unable to return contentStream", renditionType);
        }
        return contentStream;
    }

    private ContentStream getWebRenditionContentStream(Document document) {
        ContentStream webRenditionContentStream = null;
        if (document.getRenditions() != null) {
            List<Rendition> renditions = document.getRenditions().stream()
                    .filter(rendition -> !rendition.getKind().equals(THUMBNAIL_KIND))
                    .collect(Collectors.toList());

            if (renditions.size() > 1) {
                log.warn("There are {} 'Web' renditions available for document '{}', returning the first one", document.getId(), renditions.size());
            }
            if (!renditions.isEmpty()) {
                final String renditionStreamId = document.getRenditions().get(0).getStreamId();
                webRenditionContentStream = document.getContentStream(renditionStreamId);
            }
        }

        return webRenditionContentStream;
    }


    private String generateServedFileName(String originalFileName, String itemId) {
        final int indexOfFileExtension = originalFileName.lastIndexOf(".");
        final String fileExtension = (indexOfFileExtension != -1) ? originalFileName.substring(indexOfFileExtension) : "";
        final String fileName = (indexOfFileExtension != -1) ? itemId + fileExtension : itemId;
        return fileName;
    }

    private void serveLinkToNativePdf(HttpServletRequest request, HttpServletResponse response, String itemId, String originalFileName) {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        final String downloadUrl = HttpRequestUtils.getOriginalFileDownloadURL(itemId, request);
        try (PDDocument document = PdfUtils.createUnableToConvertToPdf(itemId, downloadUrl, originalFileName)) {
            document.save(response.getOutputStream());
        } catch (IOException e) {
            log.error("Unable to create LinkToNativePdf file for itemId: '{}'.", itemId, e);
        }
    }


    private void addContentDispositionHeaderToResponse(ContentDispositionType contentDispositionType, HttpServletResponse response, String filename) {
        switch (contentDispositionType) {
            case none:
                //Do nothing, let the browser handle it
                break;
            case inline:
                response.setHeader(ControllerConstants.Headers.CONTENT_DISPOSITION, "inline;filename=\"" + filename + "\"");
                break;
            case attachment:
                response.setHeader(ControllerConstants.Headers.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"");
                break;
        }
    }
}
