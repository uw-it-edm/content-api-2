package edu.uw.edm.contentapi2.controller.documentation.v1;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.uw.edm.contentapi2.controller.content.v1.UserController;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.properties.SecurityProperties;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maxime Deravet Date: 7/13/18
 */
@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(controllers = UserController.class)
public class UserDocumentationTest {

    @Autowired
    private MockMvc mvc;


    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;

    @Before
    public void setup() {

    }

    @Test
    public void listUsers() throws Exception {
        this.mvc.perform(
                get("/content/v1/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "me")
        )
                .andExpect(status().isOk())
                .andDo(this.documentationResultHandler.document(
                        relaxedResponseFields(
                                fieldWithPath("userName").description("current Logged User"),
                                fieldWithPath("accounts[]").description("accounts"),
                                fieldWithPath("roles[]").description("roles")
                        )));
    }
}
