package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.repository.acs.openapi.SearchResultTransformer;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchResultTransformerImplTest {

    @Mock
    ProfileFacade profileFacade;

    SearchResultTransformer searchResultTransformer;

    User user = new User("test-user", "", Collections.emptyList());


    @Before
    public void setUp() {
        searchResultTransformer = new SearchResultTransformerImpl(profileFacade);
    }


    @Test
    public void toContentApiDocument() {
        assertThat("this", is("tested"));
    }
}