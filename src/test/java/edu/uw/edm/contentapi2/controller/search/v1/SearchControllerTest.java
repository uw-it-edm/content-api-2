package edu.uw.edm.contentapi2.controller.search.v1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFacade documentFacade;

    @MockBean
    private FieldMapper fieldMapper;

    @Test
    public void searchInIndex() throws Exception {
        final String searchQuery =
                "{" +
                        "\"query\":\"test\"," +
                        "\"facets\":[" +
                        "{" +
                        "\"field\":\"metadata.TestField1.raw\"," +
                        "\"size\":50" +
                        "},{" +
                        "\"field\":\"metadata.TestField2.raw\"," +
                        "\"size\":20" +
                        "},{" +
                        "\"field\":\"metadata.TestYear\"," +
                        "\"size\":50" +
                        "}]," +
                        "\"filters\":[" +
                        "{" +
                        "\"label\":\"2019\"," +
                        "\"field\":\"metadata.TestYear\"," +
                        "\"term\":\"2019\"," +
                        "\"type\":\"year\"" +
                        "}]," +
                        "\"from\":0," +
                        "\"page\":0," +
                        "\"pageSize\":50," +
                        "\"searchOrder\":{" +
                        "\"order\":\"desc\"," +
                        "\"term\":\"id\"" +
                        "}" +
                        "}";


        this.mockMvc.perform(post("/search/v1/testProfile")
                .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "test-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(searchQuery))
                .andExpect(status().isOk());

        final List<SearchFilter> expectedFilters = Arrays.asList(new SimpleSearchFilter("metadata.TestYear", "2019", false));
        final SearchOrder expectedSearchOrder = new SearchOrder("id", Order.desc);

        final ArgumentCaptor<SearchQueryModel> argument = ArgumentCaptor.forClass(SearchQueryModel.class);
        verify(documentFacade, times(1)).searchDocuments(eq("testProfile"), argument.capture(), any(User.class));

        assertEquals("test", argument.getValue().getQuery());
        assertEquals(expectedFilters, argument.getValue().getFilters());
        assertEquals(expectedSearchOrder, argument.getValue().getSearchOrder());
        assertEquals(0, argument.getValue().getFrom());
        assertEquals(50, argument.getValue().getPageSize());
        assertEquals(3, argument.getValue().getFacets().size());
        assertEquals("metadata.TestField1.raw", argument.getValue().getFacets().get(0).getField());
        assertEquals(50, argument.getValue().getFacets().get(0).getSize());
        assertEquals("metadata.TestField2.raw", argument.getValue().getFacets().get(1).getField());
        assertEquals(20, argument.getValue().getFacets().get(1).getSize());
        assertEquals("metadata.TestYear", argument.getValue().getFacets().get(2).getField());
        assertEquals(50, argument.getValue().getFacets().get(2).getSize());
    }
}