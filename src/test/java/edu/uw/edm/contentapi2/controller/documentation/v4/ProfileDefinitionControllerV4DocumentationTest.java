package edu.uw.edm.contentapi2.controller.documentation.v4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.controller.content.v4.ProfileDefinitionControllerV4;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

import static edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest.GENERATED_SNIPPETS_BASE_PATH;
import static edu.uw.edm.contentapi2.controller.documentation.v4.ProfileDefinitionControllerV4DocumentationTest.V4_SNIPPETS_PATH;
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
 * @author Maxime Deravet Date: 7/17/18
 */
@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(
        controllers = ProfileDefinitionControllerV4.class,
        outputDir = V4_SNIPPETS_PATH)
public class ProfileDefinitionControllerV4DocumentationTest {

    static final String V4_SNIPPETS_PATH = GENERATED_SNIPPETS_BASE_PATH + "/v4";

    private static final String CONTEXT_PATH = "";

    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProfileFacade profileFacade;

    @Test
    public void getProfileDefinition() throws Exception {
        ProfileDefinitionV4 profileDefinitionV4 = getTestProfileDefinition();

        doReturn(profileDefinitionV4).when(profileFacade).getProfileDefinition(eq("my-profile"), any(User.class));

        this.mockMvc.perform(
                get(CONTEXT_PATH + "/content/v4/{profile-name}/profile", "my-profile")
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("profile-name").description("The name of the profile you want to get the definition of")
                        ),
                        requestHeaders(
                                headerWithName(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER).description("authentication header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("The definition of the id field").type("FieldDefinition"),
                                fieldWithPath("id.repoFieldName").description("Name of the field in the repository"),
                                fieldWithPath("id.type").description("The type of the field"),
                                fieldWithPath("id.validator").description("The type of validator for the field").type("String"),
                                fieldWithPath("label").description("The definition of the label field"),
                                fieldWithPath("metadata").description("Map containing the definition of all the available fields").type("Map<String,FieldDefinition>")
                        )
                ));

    }

    private ProfileDefinitionV4 getTestProfileDefinition() {


        HashMap<String, FieldDefinition> metadataDefinitions = new HashMap<>();

        metadataDefinitions.put("numberField", getDefinition("repoNumberField", MappingType.integer));
        metadataDefinitions.put("dateField", getDefinition("repoDateField", MappingType.date));
        metadataDefinitions.put("boolField", getDefinition("repoBoolField", MappingType.bool));
        metadataDefinitions.put("stringField", getDefinition("repoStringField", MappingType.string));


        return ProfileDefinitionV4.builder()
                .profile("profile-name")
                .id(getDefinition("repoId", MappingType.string))
                .label(getDefinition("repoLabel", MappingType.string))
                .metadata(metadataDefinitions)
                .build();

    }

    private FieldDefinition getDefinition(String repoFieldName, MappingType type) {
        return FieldDefinition.builder()
                .repoFieldName(repoFieldName)
                .type(type)
                .build();
    }

}
