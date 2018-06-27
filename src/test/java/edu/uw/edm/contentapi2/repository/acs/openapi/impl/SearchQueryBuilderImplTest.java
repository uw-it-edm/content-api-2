package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestSortDefinition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileDefinitionService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchQueryBuilderImplTest {

    @Mock
    ProfileDefinitionService profileDefinitionService;

    SearchQueryBuilderImpl searchQueryBuilder;

    QueryBody queryBody;
    SearchQueryModel searchQueryModel;

    User user = new User("test-user", "", Collections.emptyList());

    @Before
    public void setUp() {
        queryBody = new QueryBody();
        searchQueryModel = new SearchQueryModel();
        searchQueryBuilder = new SearchQueryBuilderImpl(profileDefinitionService);
    }


    @Test
    public void whenQueryThenQueryIsSentTest() {
        searchQueryModel.setQuery("myTerm:hello");
        searchQueryBuilder.addQuery(queryBody, searchQueryModel);

        assertThat(queryBody.getQuery().getQuery(), is(equalTo("myTerm:hello")));
    }


    @Test
    public void whenQueryIsEmptyThenQueryAllIsSentTest() {
        searchQueryBuilder.addQuery(queryBody, searchQueryModel);

        assertThat(queryBody.getQuery().getQuery(), is(equalTo("name:*")));
    }

    @Test
    public void defaultSortingTest() {
        searchQueryBuilder.addSorting(queryBody, searchQueryModel.getSearchOrder(), "my-profile", user);

        assertThat(queryBody.getSort().size(), is(equalTo(1)));
        assertThat(queryBody.getSort().get(0).getType(), is(equalTo(RequestSortDefinition.TypeEnum.SCORE)));
        assertThat(queryBody.getSort().get(0).getAscending(), is(false));
    }

    @Test
    public void sortByFieldTest() {
        searchQueryModel.getSearchOrder().setOrder(Order.asc);
        searchQueryModel.getSearchOrder().setTerm("myTerm");

        searchQueryBuilder.addSorting(queryBody, searchQueryModel.getSearchOrder(), "my-profile", user);

        assertThat(queryBody.getSort().size(), is(equalTo(1)));
        assertThat(queryBody.getSort().get(0).getType(), is(equalTo(RequestSortDefinition.TypeEnum.FIELD)));
        assertThat(queryBody.getSort().get(0).getField(), is(equalTo("myTerm")));
        assertThat(queryBody.getSort().get(0).getAscending(), is(true));
    }


    @Test
    public void paginationTest() {
        searchQueryModel.setFrom(25);
        searchQueryModel.setPageSize(5);

        searchQueryBuilder.addPagination(queryBody, searchQueryModel);

        assertThat(queryBody.getPaging(), is(notNullValue()));
        assertThat(queryBody.getPaging().getMaxItems(), is(5));
        assertThat(queryBody.getPaging().getSkipCount(), is(25));
    }

    @Test
    public void shouldAddSiteFilterTest() {
        searchQueryBuilder.addSiteFilter("my-profile", queryBody);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("SITE:my-profile")));
    }

    @Test
    public void noFilterToAddTest() {

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(0));
    }

    @Test
    public void add1FilterTest() {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", false));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("(=my-field:my-value)")));
    }

    @Test
    public void add1NotFilterTest() {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
    }

    @Test
    public void add2FiltersTest() {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));
        searchQueryModel.getFilters().add(new SearchFilter("my-second-field", "my-second-value", false));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(2));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
        assertThat(queryBody.getFilterQueries().get(1).getQuery(), is(equalTo("(=my-second-field:my-second-value)")));
    }

    @Test
    public void add1FilterAndProfileFilterTest() {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        searchQueryBuilder.addSiteFilter("my-profile", queryBody);

        assertThat(queryBody.getFilterQueries().size(), is(2));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
        assertThat(queryBody.getFilterQueries().get(1).getQuery(), is(equalTo("SITE:my-profile")));
    }
}