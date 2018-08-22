package edu.uw.edm.contentapi2.service.impl;

import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.FileServingService;

import static edu.uw.edm.contentapi2.controller.constants.ControllerConstants.Headers.CONTENT_DISPOSITION;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileServingServiceImplTest {

    @Mock
    private ExternalDocumentRepository externalDocumentRepository;

    @Mock
    private  HttpServletResponse mockHttpServletResponse;
    @Mock
    private Property mockProperty;
    @Mock
    private Document mockDocument;

    private ByteArrayServletOutputStream outputStream;

    private FileServingService fileServingService;

    @Before
    public void setup() throws IOException, RepositoryException {
        fileServingService = new FileServingServiceImpl(externalDocumentRepository);

        final ContentStream mockContentStream = mock(ContentStream.class);
        when(mockContentStream.getMimeType()).thenReturn("test-mime-type");
        when(mockContentStream.getLength()).thenReturn(-1L);
        when(mockContentStream.getStream()).thenReturn(new ByteArrayInputStream("This is a test stream.".getBytes()));

        final ContentStream mockContentStream2 = mock(ContentStream.class);
        when(mockContentStream2.getMimeType()).thenReturn("test-mime-type2");
        when(mockContentStream2.getLength()).thenReturn(-1L);
        when(mockContentStream2.getStream()).thenReturn(new ByteArrayInputStream("This is a test stream2.".getBytes()));

        final Rendition mockRendition = mock(Rendition.class);
        when(mockRendition.getStreamId()).thenReturn("test-stream-id-2");
        when(mockRendition.getKind()).thenReturn("pdf");

        final List<Rendition> mockRenditions = new ArrayList<>();
        mockRenditions.add(mockRendition);

        mockDocument = mock(Document.class);
        when(mockDocument.getContentStream()).thenReturn(mockContentStream);
        when(mockDocument.getContentStream("test-stream-id-2")).thenReturn(mockContentStream2);
        when(mockDocument.getRenditions()).thenReturn(mockRenditions);

        when(externalDocumentRepository.getDocumentById(eq("my-item-id"), any(User.class), any())).thenReturn(mockDocument);

        outputStream = new ByteArrayServletOutputStream();
        when(mockHttpServletResponse.getOutputStream()).thenReturn(outputStream);

    }

    @Test
    public void serveFileTest() throws IOException, RepositoryException {
        when(mockDocument.getContentStreamFileName()).thenReturn("test.png");
        fileServingService.serveFile("my-item-id", ContentRenditionType.Primary, ContentDispositionType.inline, false, mock(User.class), mock(HttpServletRequest.class),mockHttpServletResponse);

        verify(externalDocumentRepository, times(1)).getDocumentById(eq("my-item-id"), any(User.class), any());
        verify(mockHttpServletResponse,times(1)).setHeader(CONTENT_DISPOSITION,"inline;filename=\"my-item-id.png\"");
        assertEquals("This is a test stream.", new String(outputStream.toByteArray()));

    }

    @Test
    public void whenFileNameDoesNotHaveExtensionReturnDocId() throws IOException, RepositoryException {
        when(mockDocument.getContentStreamFileName()).thenReturn("test");

        fileServingService.serveFile("my-item-id", ContentRenditionType.Primary, ContentDispositionType.inline, false, mock(User.class), mock(HttpServletRequest.class),mockHttpServletResponse);

        verify(externalDocumentRepository, times(1)).getDocumentById(eq("my-item-id"), any(User.class), any());
        verify(mockHttpServletResponse,times(1)).setHeader(CONTENT_DISPOSITION,"inline;filename=\"my-item-id\"");
        assertEquals("This is a test stream.", new String(outputStream.toByteArray()));

    }

    @Test
    public void serveWebRendition() throws IOException, RepositoryException {
        when(mockDocument.getContentStreamFileName()).thenReturn("test.png");

        fileServingService.serveFile("my-item-id", ContentRenditionType.Web, ContentDispositionType.inline, false, mock(User.class), mock(HttpServletRequest.class),mockHttpServletResponse);

        verify(externalDocumentRepository, times(1)).getDocumentById(eq("my-item-id"), any(User.class), any());
        verify(mockHttpServletResponse,times(1)).setHeader(CONTENT_DISPOSITION,"inline;filename=\"my-item-id.png\"");
        assertEquals("This is a test stream2.", new String(outputStream.toByteArray()));

    }
}
