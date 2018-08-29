package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestFacetFieldsFacets;
import com.alfresco.client.api.search.body.RequestSortDefinition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFacet;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchQueryBuilderImplTest {

    @Mock
    ProfileFacade profileFacade;

    SearchQueryBuilderImpl searchQueryBuilder;

    QueryBody queryBody;
    SearchQueryModel searchQueryModel;

    User user = new User("test-user", "", Collections.emptyList());

    @Before
    public void setUp() throws NoSuchProfileException {
        queryBody = new QueryBody();
        searchQueryModel = new SearchQueryModel();
        searchQueryBuilder = new SearchQueryBuilderImpl(profileFacade);

        when(profileFacade.getRepoFQDNFieldName(anyString(), anyString(), eq(user))).then(AdditionalAnswers.returnsFirstArg());
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
    public void defaultSortingTest() throws NoSuchProfileException {
        searchQueryBuilder.addSorting(queryBody, searchQueryModel.getSearchOrder(), "my-profile", user);

        assertThat(queryBody.getSort().size(), is(equalTo(1)));
        assertThat(queryBody.getSort().get(0).getType(), is(equalTo(RequestSortDefinition.TypeEnum.SCORE)));
        assertThat(queryBody.getSort().get(0).getAscending(), is(false));
    }

    @Test
    public void sortByFieldTest() throws NoSuchProfileException {
        searchQueryModel.getSearchOrder().setOrder(Order.asc);
        searchQueryModel.getSearchOrder().setTerm("myTerm");

        searchQueryBuilder.addSorting(queryBody, searchQueryModel.getSearchOrder(), "my-profile", user);

        assertThat(queryBody.getSort().size(), is(equalTo(1)));
        assertThat(queryBody.getSort().get(0).getType(), is(equalTo(RequestSortDefinition.TypeEnum.FIELD)));
        assertThat(queryBody.getSort().get(0).getField(), is(equalTo("myTerm")));
        assertThat(queryBody.getSort().get(0).getAscending(), is(true));
    }

    @Test
    public void sortByOldSearchAPiFieldTest() throws NoSuchProfileException {
        searchQueryModel.getSearchOrder().setOrder(Order.asc);
        searchQueryModel.getSearchOrder().setTerm("metadata.myTerm.lowercase");

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
    public void noFilterToAddTest() throws NoSuchProfileException {

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(0));
    }

    @Test
    public void add1FilterTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", false));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("(=my-field:my-value)")));
    }

    @Test
    public void add1RangeFilterTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "[NOW-1DAY TO NOW+1DAY]", false));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("(my-field:[NOW-1DAY TO NOW+1DAY])")));
    }


    @Test
    public void add1NotFilterTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(1));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
    }

    @Test
    public void add2FiltersTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));
        searchQueryModel.getFilters().add(new SearchFilter("my-second-field", "my-second-value", false));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        assertThat(queryBody.getFilterQueries().size(), is(2));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
        assertThat(queryBody.getFilterQueries().get(1).getQuery(), is(equalTo("(=my-second-field:my-second-value)")));
    }

    @Test
    public void add1FilterAndProfileFilterTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("my-field", "my-value", true));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        searchQueryBuilder.addSiteFilter("my-profile", queryBody);

        assertThat(queryBody.getFilterQueries().size(), is(2));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
        assertThat(queryBody.getFilterQueries().get(1).getQuery(), is(equalTo("SITE:my-profile")));
    }

    @Test
    public void addOldSearchAPIFormatFilterTest() throws NoSuchProfileException {


        searchQueryModel.getFilters().add(new SearchFilter("metadata.my-field.raw", "my-value", true));

        searchQueryBuilder.addFilters(queryBody, searchQueryModel.getFilters(), "my-profile", user);

        searchQueryBuilder.addSiteFilter("my-profile", queryBody);

        assertThat(queryBody.getFilterQueries().size(), is(2));
        assertThat(queryBody.getFilterQueries().get(0).getQuery(), is(equalTo("!(=my-field:my-value)")));
        assertThat(queryBody.getFilterQueries().get(1).getQuery(), is(equalTo("SITE:my-profile")));
    }

    @Test
    public void add1FacetTest() throws NoSuchProfileException {

        SearchFacet searchFacet1 = new SearchFacet();
        searchFacet1.setField("metadata.my-field.raw");
        searchFacet1.setSize(20);
        searchFacet1.setSize(2);
        searchFacet1.setOrder(Order.desc);

        searchQueryModel.getFacets().add(searchFacet1);


        searchQueryBuilder.addFacets(queryBody, searchQueryModel.getFacets(), "my-profile", user);

        assertThat(queryBody.getFacetFields().getFacets().size(), is(1));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getField(), is(equalTo("my-field")));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getLabel(), is(equalTo("metadata.my-field.raw")));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getLimit(), is(equalTo(2)));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getSort(), is(equalTo(RequestFacetFieldsFacets.SortEnum.COUNT)));
    }

    @Test
    public void add2FacetTest() throws NoSuchProfileException {

        SearchFacet searchFacet1 = new SearchFacet();
        searchFacet1.setField("metadata.my-field.raw");
        searchFacet1.setSize(2);
        searchFacet1.setOrder(Order.desc);

        SearchFacet searchFacet2 = new SearchFacet();
        searchFacet2.setField("my-second-field.raw");
        searchFacet2.setSize(5);
        searchFacet2.setOrder(Order.asc);

        searchQueryModel.getFacets().add(searchFacet1);
        searchQueryModel.getFacets().add(searchFacet2);


        searchQueryBuilder.addFacets(queryBody, searchQueryModel.getFacets(), "my-profile", user);

        assertThat(queryBody.getFacetFields().getFacets().size(), is(2));

        assertThat(queryBody.getFacetFields().getFacets().get(0).getField(), is(equalTo("my-field")));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getLabel(), is(equalTo("metadata.my-field.raw")));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getLimit(), is(equalTo(2)));
        assertThat(queryBody.getFacetFields().getFacets().get(0).getSort(), is(equalTo(RequestFacetFieldsFacets.SortEnum.COUNT)));

        assertThat(queryBody.getFacetFields().getFacets().get(1).getField(), is(equalTo("my-second-field")));
        assertThat(queryBody.getFacetFields().getFacets().get(1).getLabel(), is(equalTo("my-second-field.raw")));
        assertThat(queryBody.getFacetFields().getFacets().get(1).getLimit(), is(equalTo(5)));
        assertThat(queryBody.getFacetFields().getFacets().get(1).getSort(), is(equalTo(RequestFacetFieldsFacets.SortEnum.COUNT)));
    }
}