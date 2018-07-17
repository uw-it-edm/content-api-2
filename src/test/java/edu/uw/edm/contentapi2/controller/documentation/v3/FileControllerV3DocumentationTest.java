package edu.uw.edm.contentapi2.controller.documentation.v3;

import org.apache.catalina.ssi.ByteArrayServletOutputStream;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.content.v3.FileV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.security.UserDetailsService;
import edu.uw.edm.contentapi2.service.DocumentFacade;
import edu.uw.edm.contentapi2.service.FileServingService;

import static edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest.GENERATED_SNIPPETS_BASE_PATH;
import static edu.uw.edm.contentapi2.controller.documentation.v3.ItemControllerV3DocumentationTest.V3_SNIPPETS_PATH;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maxime Deravet Date: 7/13/18
 */

@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(
        controllers = FileV3Controller.class,
        outputDir = V3_SNIPPETS_PATH,
        includeFilters = {
                @ComponentScan.Filter(classes = EnableWebSecurity.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FileServingService.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityProperties.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserDetailsService.class)
        }
)
public class FileControllerV3DocumentationTest {

    static final String V3_SNIPPETS_PATH = GENERATED_SNIPPETS_BASE_PATH + "/v3";

    private static final String CONTEXT_PATH = "";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;


    @Autowired
    FileServingService fileServingService;
    @MockBean
    ExternalDocumentRepository<Document> externalDocumentRepository;

    @MockBean
    DocumentFacade documentFacade;

    @Mock
    private HttpServletResponse response;


    @Before
    public void setUp() {
    }

    @Test
    public void readFile() throws Exception {
        ByteArrayServletOutputStream outputStream = new ByteArrayServletOutputStream();

        Document mockDocument = mock(Document.class);
        ContentStream mockContentStream = mock(ContentStream.class);

        Property title = mock(Property.class);
        doReturn(TITLE_FQDN).when(title).getId();
        doReturn("my-file.txt").when(title).getValue();


        doReturn(Arrays.asList(title)).when(mockDocument).getProperties();


        doReturn(new ByteArrayInputStream("Hello, Goodbye".getBytes()))
                .when(mockContentStream).getStream();

        doReturn("my-file")
                .when(mockContentStream).getFileName();
        doReturn(mockContentStream)
                .when(mockDocument).getContentStream();

        doReturn(mockDocument)
                .when(externalDocumentRepository).getDocumentById(eq("123"), any(User.class));


        this.mockMvc.perform(get(CONTEXT_PATH + "/content/v3/file/{itemId}", "123", response)
                .param("rendition", "Web")
                .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Goodbye"))
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("itemId").description("Identifier for the content item")
                        ),
                        requestParameters(
                                parameterWithName("rendition").description("The rendition of the document to be retrieved (e.g. Web or Primary)")
                        )
                    )
                );

    }


}
