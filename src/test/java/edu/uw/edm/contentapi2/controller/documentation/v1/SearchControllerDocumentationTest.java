package edu.uw.edm.contentapi2.controller.documentation.v1;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.HashMap;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest;
import edu.uw.edm.contentapi2.controller.search.v1.SearchControllerV1;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFacet;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.BucketResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.FacetResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.security.UserDetailsService;
import edu.uw.edm.contentapi2.service.DocumentFacade;

import static edu.uw.edm.contentapi2.controller.documentation.config.ContentAPIRestDocTest.GENERATED_SNIPPETS_BASE_PATH;
import static edu.uw.edm.contentapi2.controller.documentation.v1.SearchControllerDocumentationTest.V1_SNIPPETS_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maxime Deravet Date: 1/19/18
 */

@RunWith(SpringRunner.class)
@ContentAPIRestDocTest(
        controllers = SearchControllerV1.class,
        outputDir = V1_SNIPPETS_PATH,
        includeFilters = {
                @ComponentScan.Filter(classes = EnableWebSecurity.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityProperties.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserDetailsService.class)
        }
)
public class SearchControllerDocumentationTest {

    static final String V1_SNIPPETS_PATH = GENERATED_SNIPPETS_BASE_PATH + "/v1";
    private static final String CONTEXT_PATH = "";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public RestDocumentationResultHandler documentationResultHandler;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    DocumentFacade documentFacade;


    @Test
    public void search() throws Exception {
        SearchQueryModel searchQueryModel = getQueryModelForTest();

        String content = objectMapper.writeValueAsString(searchQueryModel);
        SearchResultContainer searchContainer = getSearchContainer();

        doReturn(searchContainer)
                .when(documentFacade)
                .searchDocuments(anyString(), any(SearchQueryModel.class), any(User.class));


        this.mockMvc.perform(
                MockMvcRequestBuilders
                        .post(CONTEXT_PATH + "/search/v1/{Profile}", "Profile")
                        .header(SecurityProperties.DEFAULT_AUTHENTICATION_HEADER, "my-auth-header")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(documentationResultHandler.document(
                        pathParameters(
                                parameterWithName("Profile").description("The profile you are searching on")
                        ),
                        requestFields(
                                fieldWithPath("query").description("an Elasticsearch query string query, see https://www.elastic.co/guide/en/elasticsearch/reference/2.4/query-dsl-query-string-query.html#query-string-syntax for the query field syntax"),

                                fieldWithPath("filters").description("a list of filters. Optional").optional(),
                                fieldWithPath("filters[].field").description("The field you want to filter on").optional(),
                                fieldWithPath("filters[].term").description("The value of the filter").optional(),
                                fieldWithPath("filters[].not").description("Negate the filter. Optional").optional(),

                                fieldWithPath("facets").description("a list of facets. Optional").optional(),
                                fieldWithPath("facets[].field").description("The field you want to facet on"),
                                fieldWithPath("facets[].size").description("Maximum number of facets"),
                                fieldWithPath("facets[].order").description("Order for the facet. Optional").optional(),

                                fieldWithPath("searchOrder").description("the Search Order. Optional, defaults is `_score` `desc` meaning that most relevant results are at the top").optional(),
                                fieldWithPath("searchOrder.term").description("The field you want to order on").optional(),
                                fieldWithPath("searchOrder.order").description("The order desc/asc").optional(),
                                fieldWithPath("searchOrder.missing").description("deprecated").optional(),
                                fieldWithPath("searchOrder.script").description("deprecated").optional(),

                                fieldWithPath("from").description("request results starting at this index. Optional, defaults to 0").optional(),
                                fieldWithPath("pageSize").description("The Page Size. Optional, defaults to 20").optional()
                        ),

                        relaxedResponseFields(
                                fieldWithPath("searchResults").description("The list of documents matching the search"),
                                fieldWithPath("searchResults").description("The list of documents matching the search"),
                                fieldWithPath("facets").description("The Facets you requested"),
                                fieldWithPath("totalCount").description("The total number of documents for this search"),
                                fieldWithPath("timeTaken").description("The time it took for the api to execute the search")
                        )));
    }

    private SearchResult getTestSearchResults(String suffix) {
        SearchResult searchResult = new SearchResult();

        searchResult.setIndexName("documents-myprofile");

        searchResult.setDocument(getSearchResultDocument(suffix));
        searchResult.setScore(1.0f);
        return searchResult;
    }

    private SearchResultContainer getSearchContainer() {
        SearchResultContainer searchContainer = new SearchResultContainer();


        searchContainer.setSearchResults(Arrays.asList(getTestSearchResults("1"), getTestSearchResults("2")));
        searchContainer.setTimeTaken("1.0");
        searchContainer.setTotalCount(1);
        searchContainer.setFacets(Arrays.asList(getFacetResult()));
        return searchContainer;
    }

    public FacetResult getFacetResult() {
        FacetResult facetResult = new FacetResult("metadata.myStringField.raw");
        facetResult.setBuckets(Arrays.asList(new BucketResult(1, "value 1"), new BucketResult(1, "value 2")));
        return facetResult;
    }

    private ContentAPIDocument getSearchResultDocument(String suffix) {
        ContentAPIDocument document = new ContentAPIDocument();
        document.setId("1234" + suffix);
        document.setLabel("label " + suffix);
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("myNumberField", 1231);
        metadata.put("myStringField", "value " + suffix);
        metadata.put("myStringFilteredField", "filteredValue");
        metadata.put("myNumberFilteredField", 1985);
        document.setMetadata(metadata);
        return document;
    }

    private SearchQueryModel getQueryModelForTest() {
        SearchQueryModel searchQueryModel = new SearchQueryModel();
        SimpleSearchFilter stringSearchFilter = new SimpleSearchFilter();
        stringSearchFilter.setField("metadata.myStringFilteredField.raw");
        stringSearchFilter.setTerm("value");
        stringSearchFilter.setNot(false);

        SimpleSearchFilter numberSearchFilter = new SimpleSearchFilter();
        numberSearchFilter.setField("metadata.myNumberFilteredField");
        numberSearchFilter.setTerm("1985");
        numberSearchFilter.setNot(false);

        searchQueryModel.setFilters(Arrays.asList(stringSearchFilter, numberSearchFilter));

        searchQueryModel.setQuery("elasticsearch Query String Query");

        searchQueryModel.setSearchOrder(new SearchOrder("metadata.myStringField.lowercase", Order.desc));

        searchQueryModel.setFrom(0);
        searchQueryModel.setPageSize(50);

        SearchFacet searchFacet = new SearchFacet();
        searchFacet.setField("metadata.myStringField.raw");
        searchFacet.setSize(10);
        searchFacet.setOrder(Order.desc);

        searchQueryModel.setFacets(Arrays.asList(searchFacet));

        return searchQueryModel;
    }


}
