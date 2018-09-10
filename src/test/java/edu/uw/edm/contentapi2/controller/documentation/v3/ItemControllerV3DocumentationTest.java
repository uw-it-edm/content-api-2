package edu.uw.edm.contentapi2.controller.documentation.v3;

import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.util.Arrays;

import edu.uw.edm.contentapi2.controller.content.v3.ItemV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.Conjunction;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchOrder;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static edu.uw.edm.contentapi2.TestUtilities.getTestDocument;
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

    @Test
    public void searchItems() throws Exception {
        final LegacySearchModel searchModel = new LegacySearchModel();
        searchModel.setConjunction(Conjunction.and);
        searchModel.setSearch(Arrays.asList(
                "ProfileId=test-profile",
                "test-field=123*",
                "test-field-2={gte}1;test-field-2={lt}20"));
        searchModel.setOrder(SearchOrder.asc);
        searchModel.setOrderBy("id");

        final DocumentSearchResults documentSearchResults = DocumentSearchResults.builder()
                .totalCount(0)
                .build();

        doReturn(documentSearchResults)
                .when(documentFacade)
                .searchDocuments(any(LegacySearchModel.class), any(User.class));

        this.mockMvc.perform(MockMvcRequestBuilders.post(CONTEXT_PATH + "/content/v3/item/_search")
                .content(objectMapper.writeValueAsBytes(searchModel))
                .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        relaxedResponseFields(
                                fieldWithPath("documents").description("An array containing the documents that matched your query"),
                                fieldWithPath("totalCount").description("Total number of documents for your search"),
                                fieldWithPath("resultListSize").description("Number of documents in the current page")
                        )));
    }
}
