package edu.uw.edm.contentapi2.controller.documentation.v3;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import edu.uw.edm.contentapi2.controller.content.v3.BulkV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.ContentApiProperties;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static edu.uw.edm.contentapi2.TestUtilities.getTestDocument;
import static edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest.GENERATED_SNIPPETS_BASE_PATH;
import static edu.uw.edm.contentapi2.controller.documentation.v3.BulkContollerV3DocumentationTest.V3_SNIPPETS_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(
        controllers = BulkV3Controller.class,
        outputDir = V3_SNIPPETS_PATH)
public class BulkContollerV3DocumentationTest {
    static final String V3_SNIPPETS_PATH = GENERATED_SNIPPETS_BASE_PATH + "/v3";

    private static final String CONTEXT_PATH = "";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;

    @MockBean
    ContentApiProperties contentApiProperties;
    @MockBean
    DocumentFacade documentFacade;

    @Before
    public void setup() {
        doReturn(10)
                .when(contentApiProperties)
                .getBulkUpdateMaxItems();
    }

    @Test
    public void updateItems() throws Exception {
        final List<ContentAPIDocument> testDocuments = new ArrayList<>();
        testDocuments.add(getTestDocument("success-id-1"));
        testDocuments.add(getTestDocument("success-id-2"));

        final String content = objectMapper.writeValueAsString(testDocuments);

        when(documentFacade.updateDocument(startsWith("success-id"), any(ContentAPIDocument.class), any(User.class)))
                .thenAnswer(i -> i.getArguments()[1]);//return second argument

        this.mockMvc.perform(

                post(CONTEXT_PATH + "/content/v3/bulk/item")
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(UTF_8.displayName())
                        .content(content))
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        relaxedResponseFields(
                                fieldWithPath("successes").description("the updated documents"),
                                fieldWithPath("failures").description("the documents in error")
                        )));

    }


}
