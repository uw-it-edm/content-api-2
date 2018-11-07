package edu.uw.edm.contentapi2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.model.Conjunction;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchOrder;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maxime Deravet Date: 3/27/18
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemV3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;
    @MockBean
    private FieldMapper fieldMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deleteItemIdTest() throws Exception {
        ContentAPIDocument value = new ContentAPIDocument();
        value.setId("my-item-id");

        this.mockMvc.perform(delete("/content/v3/item/my-item-id").header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "test-user"))
                .andExpect(status().isOk());

        verify(documentFacade, times(1)).deleteDocumentById(eq("my-item-id"), any(User.class));
    }


    @Test
    public void getItemIdTest() throws Exception {
        ContentAPIDocument value = new ContentAPIDocument();
        value.setId("my-item-id");
        when(documentFacade.getDocumentById(eq("my-item-id"), any(User.class))).thenReturn(value);

        this.mockMvc.perform(get("/content/v3/item/my-item-id").header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "test-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.id").value("my-item-id"));
    }

    @Test
    public void searchItemTest() throws Exception {
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

        when(documentFacade.searchDocuments(any(LegacySearchModel.class), any(User.class)))
                .thenReturn(documentSearchResults);

        this.mockMvc.perform(post("/content/v3/item/_search")
                .content(objectMapper.writeValueAsBytes(searchModel))
                .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(documentFacade, times(1)).searchDocuments(eq(searchModel), any(User.class));
    }

}
