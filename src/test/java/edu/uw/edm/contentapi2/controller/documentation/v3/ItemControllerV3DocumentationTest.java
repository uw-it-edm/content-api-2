package edu.uw.edm.contentapi2.controller.documentation.v3;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

import edu.uw.edm.contentapi2.controller.content.v3.ItemV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest.GENERATED_SNIPPETS_BASE_PATH;
import static edu.uw.edm.contentapi2.controller.documentation.v3.ItemControllerV3DocumentationTest.V3_SNIPPETS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maxime Deravet Date: 7/13/18
 */

@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(
        controllers = ItemV3Controller.class,
        outputDir = V3_SNIPPETS_PATH)
public class ItemControllerV3DocumentationTest {

    static final String V3_SNIPPETS_PATH = GENERATED_SNIPPETS_BASE_PATH + "/v3";

    private static final String CONTEXT_PATH = "";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;


    @MockBean
    DocumentFacade documentFacade;

    @Before
    public void setup() {

    }

    @Test
    public void getItemMetadata() throws Exception {
        ContentAPIDocument testDocument = getTestDocument();

        doReturn(testDocument).when(documentFacade).getDocumentById(eq("123"), any(User.class));


        this.mockMvc.perform(
                get(CONTEXT_PATH + "/content/v3/item/{itemId}", "123")
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("itemId").description("Identifier for the content item")
                        ),
                        requestHeaders(
                                headerWithName(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER).description("authentication header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("The id of the document"),
                                fieldWithPath("label").description("The label of the document"),
                                fieldWithPath("metadata").description("An object containing all the metadata available for your document")
                        )
                ));

    }


    @Test
    public void updateItem() throws Exception {
        MockMultipartFile attachment = new MockMultipartFile("attachment", "myfile.cad", "application/pdf", "yourcadfile".getBytes());
        //MockMultipartFile alternate = new MockMultipartFile("alternate", "myfile.pdf", "application/pdf", "yourpdffile".getBytes());

        ContentAPIDocument testDocument = getTestDocument();

        String content = objectMapper.writeValueAsString(testDocument);
        MockMultipartFile document = new MockMultipartFile("document", "json", "application/json", content.getBytes());

        doReturn(testDocument)
                .when(documentFacade)
                .updateDocument(
                        eq("123"),
                        any(ContentAPIDocument.class),
                        any(MultipartFile.class),
                        any(User.class));

        this.mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart(CONTEXT_PATH + "/content/v3/item/{itemId}", testDocument.getId())
                        .file(document)
                        .file(attachment)
                        //.file(alternate)
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)

        )
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        relaxedResponseFields(
                                fieldWithPath("id").description("The id of the updated document"),
                                fieldWithPath("label").description("The label of the document"),
                                fieldWithPath("metadata").description("An object containing all the metadata available for your document")
                        )));

    }

    @Test
    public void createItem() throws Exception {
        MockMultipartFile attachment = new MockMultipartFile("attachment", "myfile.cad", "application/cad", "yourcadfile".getBytes());
        MockMultipartFile alternate = new MockMultipartFile("alternate", "myfile.pdf", "application/pdf", "yourpdffile".getBytes());

        ContentAPIDocument testDocumentResponse = getTestDocument();
        ContentAPIDocument testDocumentRequest = getTestDocument();
        testDocumentRequest.setId(null);

        MockMultipartFile document = new MockMultipartFile("document", "json", "application/json", objectMapper.writeValueAsString(testDocumentRequest).getBytes());

        doReturn(testDocumentResponse)
                .when(documentFacade)
                .createDocument(
                        any(ContentAPIDocument.class),
                        any(MultipartFile.class),
                        any(User.class));

        this.mockMvc.perform(
                MockMvcRequestBuilders
                        .multipart(CONTEXT_PATH + "/content/v3/item")
                        .file(document)
                        .file(attachment)
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        relaxedResponseFields(
                                fieldWithPath("id").description("The id of the created document"),
                                fieldWithPath("label").description("The label of the document"),
                                fieldWithPath("metadata").description("An object containing all the metadata available for your document")
                        )));

    }


    private ContentAPIDocument getTestDocument() {
        return getTestDocument("123");
    }


    private ContentAPIDocument getTestDocument(String documentId) {
        ContentAPIDocument document123 = new ContentAPIDocument();


        document123.setId(documentId);
        document123.setLabel("document" + documentId + " title");

        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("ProfileId", "The Profile of the document");
        metadata.put("Account", "The Account of the document");
        metadata.put("OriginalFileName", "SamplePDF3217335768660836293.pdf");
        metadata.put("WebExtension", "pdf");
        metadata.put("Other", "any other metadata available in your documents");

        document123.setMetadata(metadata);
        return document123;
    }


}