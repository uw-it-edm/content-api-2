package edu.uw.edm.contentapi2.repository.acs.openapi;

import com.alfresco.client.AlfrescoClient;
import com.alfresco.client.api.search.SearchAPI;
import com.alfresco.client.api.search.body.QueryBody;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.repository.acs.openapi.impl.SearchQueryBuilderImpl;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 7/18/18
 */
@RunWith(MockitoJUnitRunner.class)
public class ACSSearchRepositoryImplTest {

    private ACSSearchRepositoryImpl searchRepository;

    private User user = new User("test-user", "", Collections.emptyList());

    @Mock
    private SearchQueryBuilderImpl searchQueryBuilder;
    @Mock
    private SearchResultTransformer searchResultTransformer;

    private String mockResponse = "{\n" +
            "    \"list\": {\n" +
            "        \"pagination\": {\n" +
            "            \"count\": 2,\n" +
            "            \"hasMoreItems\": false,\n" +
            "            \"totalItems\": 2,\n" +
            "            \"skipCount\": 0,\n" +
            "            \"maxItems\": 50\n" +
            "        },\n" +
            "        \"context\": {\n" +
            "            \"consistency\": {\n" +
            "                \"lastTxId\": 123213\n" +
            "            },\n" +
            "            \"facetsFields\": [\n" +
            "                {\n" +
            "                    \"label\": \"my-faceted-field\",\n" +
            "                    \"buckets\": [\n" +
            "                        {\n" +
            "                            \"label\": \"Value1\",\n" +
            "                            \"filterQuery\": \"my:field:\\\"Value1\\\"\",\n" +
            "                            \"count\": 1\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"label\": \"Value2\",\n" +
            "                            \"filterQuery\":  \"my:field:\\\"Value2\\\"\",\n" +
            "                            \"count\": 1\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"entries\": [\n" +
            "            {\n" +
            "                \"entry\": {\n" +
            "                    \"isFile\": true,\n" +
            "                    \"createdByUser\": {\n" +
            "                        \"id\": \"admin\",\n" +
            "                        \"displayName\": \"Administrator\"\n" +
            "                    },\n" +
            "                    \"modifiedAt\": \"2018-07-13T20:21:34.338+0000\",\n" +
            "                    \"nodeType\": \"my:type\",\n" +
            "                    \"content\": {\n" +
            "                        \"mimeType\": \"application/pdf\",\n" +
            "                        \"mimeTypeName\": \"Adobe PDF Document\",\n" +
            "                        \"sizeInBytes\": 477240,\n" +
            "                        \"encoding\": \"UTF-8\"\n" +
            "                    },\n" +
            "                    \"parentId\": \"6e28def5-a248-4ac4-b610-365a04d3953d\",\n" +
            "                    \"aspectNames\": [\n" +
            "                        \"rn:renditioned\",\n" +
            "                        \"cm:versionable\",\n" +
            "                        \"cm:titled\",\n" +
            "                        \"cm:auditable\",\n" +
            "                        \"cm:author\",\n" +
            "                        \"cm:thumbnailModification\"\n" +
            "                    ],\n" +
            "                    \"createdAt\": \"2018-07-12T18:16:21.000+0000\",\n" +
            "                    \"isFolder\": false,\n" +
            "                    \"search\": {\n" +
            "                        \"score\": 1.0\n" +
            "                    },\n" +
            "                    \"modifiedByUser\": {\n" +
            "                        \"id\": \"user1\",\n" +
            "                        \"displayName\": \"User One\"\n" +
            "                    },\n" +
            "                    \"name\": \"34567.pdf\",\n" +
            "                    \"location\": \"nodes\",\n" +
            "                    \"id\": \"789984d6-3f52-4cca-afd5-e512c2b37c2a\",\n" +
            "                    \"properties\": {\n" +
            "                        \"cm:title\": \"34567.pdf\",\n" +
            "                        \"my:field\": \"Value1\",\n" +
            "                        \"cm:versionType\": \"MAJOR\",\n" +
            "                        \"cm:versionLabel\": \"1.0\",\n" +
            "                        \"cm:lastThumbnailModification\": [\n" +
            "                            \"doclib:1531419543581\"\n" +
            "                        ]\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"entry\": {\n" +
            "                    \"isFile\": true,\n" +
            "                    \"createdByUser\": {\n" +
            "                        \"id\": \"admin\",\n" +
            "                        \"displayName\": \"Administrator\"\n" +
            "                    },\n" +
            "                    \"modifiedAt\": \"2018-07-18T22:24:06.027+0000\",\n" +
            "                    \"nodeType\": \"my:type\",\n" +
            "                    \"content\": {\n" +
            "                        \"mimeType\": \"application/pdf\",\n" +
            "                        \"mimeTypeName\": \"Adobe PDF Document\",\n" +
            "                        \"sizeInBytes\": 255727,\n" +
            "                        \"encoding\": \"UTF-8\"\n" +
            "                    },\n" +
            "                    \"parentId\": \"6e28def5-a248-4ac4-b610-365a04d3953d\",\n" +
            "                    \"aspectNames\": [\n" +
            "                        \"rn:renditioned\",\n" +
            "                        \"cm:versionable\",\n" +
            "                        \"cm:titled\",\n" +
            "                        \"cm:auditable\",\n" +
            "                        \"cm:author\",\n" +
            "                        \"cm:thumbnailModification\"\n" +
            "                    ],\n" +
            "                    \"createdAt\": \"2018-07-13T23:34:20.000+0000\",\n" +
            "                    \"isFolder\": false,\n" +
            "                    \"search\": {\n" +
            "                        \"score\": 1.0\n" +
            "                    },\n" +
            "                    \"modifiedByUser\": {\n" +
            "                        \"id\": \"user2\",\n" +
            "                        \"displayName\": \"User Two\"\n" +
            "                    },\n" +
            "                    \"name\": \"123235.pdf\",\n" +
            "                    \"location\": \"nodes\",\n" +
            "                    \"id\": \"cdae64f1-4fae-4a6b-85e4-04e1628d814b\",\n" +
            "                    \"properties\": {\n" +
            "                        \"cm:title\": \"123235.pdf\",\n" +
            "                        \"my:field\": \"Value2\",\n" +
            "                        \"cm:versionType\": \"MAJOR\",\n" +
            "                        \"cm:versionLabel\": \"1.0\",\n" +
            "                        \"cmis:lastModifiedBy\": \"user2\",\n" +
            "                        \"cm:lastThumbnailModification\": [\n" +
            "                            \"doclib:1531952238989\"\n" +
            "                        ]\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";



    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start();


        SearchAPI searchAPI = new AlfrescoClient.Builder()
                .connect(mockWebServer.url("/").toString(), "", "")
                .build()
                .getSearchAPI();


        searchRepository = new ACSSearchRepositoryImpl(searchAPI, searchQueryBuilder, searchResultTransformer);
    }


    @Test
    public void searchQueryBuilderIsCalledTest() throws RepositoryException, IOException {


        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(mockResponse));


        SearchQueryModel searchModel = new SearchQueryModel();
        when(searchQueryBuilder.addFacets(any(), any(), any(), any())).thenReturn(new QueryBody());


        searchRepository.searchDocuments("my-profile", searchModel, user);


        verify(searchQueryBuilder, times(1)).addQuery(any(QueryBody.class), eq(searchModel));
        verify(searchQueryBuilder, times(1)).addFilters(any(), eq(searchModel.getFilters()), eq("my-profile"), eq(user));
        verify(searchQueryBuilder, times(1)).addContentModelFilter(eq("my-profile"), any());
        verify(searchQueryBuilder, times(1)).addPagination(any(), eq(searchModel));
        verify(searchQueryBuilder, times(1)).addSorting(any(), eq(searchModel.getSearchOrder()), eq("my-profile"), eq(user));
        verify(searchQueryBuilder, times(1)).addDefaultIncludedInfo(any());
        verify(searchQueryBuilder, times(1)).addFacets(any(), eq(searchModel.getFacets()), eq("my-profile"), eq(user));
    }
}