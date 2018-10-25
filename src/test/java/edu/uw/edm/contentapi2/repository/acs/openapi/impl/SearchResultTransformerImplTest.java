package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.google.gson.internal.LinkedTreeMap;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.SearchEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.repository.acs.openapi.SearchResultTransformer;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchResultTransformerImplTest {

    private static final String REPO_FIELD_1_NAME = "cm:field-1";
    private static final String CONTENT_FIELD_1_NAME = "field1";
    private static final String FIELD_1_VALUE = "field-1-value";

    @Mock
    private ProfileFacade profileFacade;

    private SearchResultTransformer searchResultTransformer;

    private User user = new User("test-user", "", Collections.emptyList());


    @Before
    public void setUp() {
        searchResultTransformer = new SearchResultTransformerImpl(profileFacade);
    }


    @Test
    public void toContentApiDocument() throws NoSuchProfileException {
        final Map<String, Object> convertedMetaData = new HashMap<>();
        convertedMetaData.put(CONTENT_FIELD_1_NAME, FIELD_1_VALUE);
        convertedMetaData.put(RepositoryConstants.ContentAPI.PROFILE_ID, "my-profile");
        when(profileFacade.convertMetadataToContentApiDataTypes(any(ResultNodeRepresentation.class), any(User.class), eq("my-profile"))).thenReturn(convertedMetaData);

        final LinkedTreeMap<String, Object> properties = new LinkedTreeMap<>();
        properties.put(RepositoryConstants.Alfresco.AlfrescoFields.TITLE_FQDN, "my-title");
        properties.put(REPO_FIELD_1_NAME, FIELD_1_VALUE);

        final SearchEntry searchEntry = new SearchEntry();
        searchEntry.setScore(9000f);

        final ResultNodeRepresentation resultNode = new ResultNodeRepresentation();
        resultNode.setProperties(properties);
        resultNode.setId("my-id");
        resultNode.setSearch(searchEntry);


        final SearchResult result = searchResultTransformer.toSearchResult(resultNode, "my-profile", user);

        assertThat(result.getDocument().getId(), is("my-id"));
        assertThat(result.getDocument().getLabel(), is("my-title"));
        assertThat(result.getDocument().getMetadata().get(CONTENT_FIELD_1_NAME), is(FIELD_1_VALUE));
        assertThat(result.get_score(), is(9000f));
    }
}