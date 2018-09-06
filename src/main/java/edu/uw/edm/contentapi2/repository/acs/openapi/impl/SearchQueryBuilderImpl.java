package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.google.common.base.Strings;

import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.body.RequestFacetFields;
import com.alfresco.client.api.search.body.RequestFacetFieldsFacets;
import com.alfresco.client.api.search.body.RequestFilterQuery;
import com.alfresco.client.api.search.body.RequestPagination;
import com.alfresco.client.api.search.body.RequestQuery;
import com.alfresco.client.api.search.body.RequestSortDefinition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.ComplexSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFacet;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.repository.acs.openapi.SearchQueryBuilder;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
@Service
public class SearchQueryBuilderImpl implements SearchQueryBuilder {

    public static final String SEARCH_IN_TERM_TOKEN = ":";
    public static final String TERM_EQUALS_TOKEN = "=";
    public static final String NOT_TOKEN = "!";
    public static final String SITE_TERM_QUERY = "SITE:";

    public static final List<String> DEFAULT_INCLUDED_FIELDS = Arrays.asList("properties", "aspectNames");
    public static final String TYPE_DOCUMENT_QUERY = "TYPE:\"cm:content\"";
    public static final String RAW_SUFFIX = ".raw";
    public static final String LOWERCASE_SUFFIX = ".lowercase";
    public static final String METADATA_PREFIX = "metadata.";


    public static final String MATCH_ALL_QUERY = "name:*";
    public static final String EMPTY_STRING = "";

    private static final Pattern rangeSearchPattern = Pattern.compile("[\\[<].*[tT][oO].*[\\]>]");


    private ProfileFacade profileFacade;


    @Autowired
    public SearchQueryBuilderImpl(ProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }

    @Override
    public QueryBody addQuery(QueryBody queryBody, SearchQueryModel searchModel) {
        if (!Strings.isNullOrEmpty(searchModel.getQuery())) {
            queryBody.query(new RequestQuery().query(searchModel.getQuery()));
        } else {
            queryBody.query(new RequestQuery().query(MATCH_ALL_QUERY));
        }

        return queryBody;
    }

    @Override
    public QueryBody addSorting(QueryBody queryBody, SearchOrder searchOrder, String profile, User user) throws NoSuchProfileException {
        RequestSortDefinition requestSortDefinition = new RequestSortDefinition();
        if (SearchOrder.SCORE_ORDER.equals(searchOrder.getTerm())) {
            requestSortDefinition.setType(RequestSortDefinition.TypeEnum.SCORE);
        } else {
            requestSortDefinition.setType(RequestSortDefinition.TypeEnum.FIELD);
            requestSortDefinition.setField(getFieldNameForACSQuery(profile, searchOrder.getTerm(), user));
        }

        requestSortDefinition.setAscending(searchOrder.getOrder() == Order.asc);


        queryBody.setSort(Collections.singletonList(requestSortDefinition));

        return queryBody;
    }

    @Override
    public QueryBody addPagination(QueryBody queryBody, SearchQueryModel searchModel) {
        queryBody.setPaging(new RequestPagination()
                .maxItems(searchModel.getPageSize())
                .skipCount(searchModel.getFrom()));

        return queryBody;
    }


    @Override
    public QueryBody addFilters(QueryBody queryBody, List<SearchFilter> searchFilters, String profileId, User user) throws NoSuchProfileException {
        List<RequestFilterQuery> filters = new ArrayList<>();

        for (SearchFilter searchFilter : searchFilters) {
            RequestFilterQuery requestFilterQuery = toRequestFilterQuery(searchFilter, profileId, user);
            filters.add(requestFilterQuery);
        }
        queryBody.setFilterQueries(filters);

        return queryBody;
    }

    @Override
    public QueryBody addFacets(QueryBody queryBody, List<SearchFacet> facets, String profile, User user) throws NoSuchProfileException {

        RequestFacetFields requestFacetFields = new RequestFacetFields();

        for (SearchFacet facet : facets) {

            RequestFacetFieldsFacets facetsItem = toRequestFacetField(profile, user, facet);

            requestFacetFields.addFacetsItem(facetsItem);
        }


        queryBody.setFacetFields(requestFacetFields);

        return queryBody;
    }

    private RequestFacetFieldsFacets toRequestFacetField(String profile, User user, SearchFacet facet) throws NoSuchProfileException {
        RequestFacetFieldsFacets facetsItem = new RequestFacetFieldsFacets();


        facetsItem.setField(getFieldNameForACSQuery(profile, facet.getField(), user));
        facetsItem.setLabel(facet.getField());

        //TODO cannot select order with ACS.
        facetsItem.setSort(RequestFacetFieldsFacets.SortEnum.COUNT);
        facetsItem.mincount(1);
        facetsItem.setLimit(facet.getSize());

        return facetsItem;
    }

    @Override
    public QueryBody addSiteFilter(String profile, QueryBody queryBody) {
        if (queryBody.getFilterQueries() == null) {
            queryBody.setFilterQueries(new ArrayList<>());
        }

        queryBody.getFilterQueries().add(new RequestFilterQuery().query(SITE_TERM_QUERY + profile));

        return queryBody;
    }

    @Override
    public QueryBody addDefaultIncludedInfo(QueryBody queryBody) {
        queryBody.setInclude(DEFAULT_INCLUDED_FIELDS);

        return queryBody;
    }

    @Override
    public QueryBody addIsDocumentFilter(String profile, QueryBody queryBody) {
        queryBody.getFilterQueries().add(new RequestFilterQuery().query(TYPE_DOCUMENT_QUERY));
        return queryBody;
    }


    private boolean isRangeQuery(String term) {
        return rangeSearchPattern.matcher(term).matches();
    }

    private RequestFilterQuery toRequestFilterQuery(SearchFilter searchFilter, String profile, User user) throws NoSuchProfileException {
        final String filterQuery = this.createSearchFilterQuery(searchFilter, profile, user);
        return new RequestFilterQuery().query(filterQuery);
    }

    private String createSearchFilterQuery(SearchFilter searchFilter, String profile, User user) throws NoSuchProfileException {
        if (searchFilter instanceof ComplexSearchFilter) {
            return this.createComplexSearchFilterQuery((ComplexSearchFilter) searchFilter, profile, user);

        } else {
            return this.createSimpleSearchFilterQuery((SimpleSearchFilter) searchFilter, profile, user);
        }
    }

    private String createSimpleSearchFilterQuery(SimpleSearchFilter simpleSearchFilter, String profile, User user) throws NoSuchProfileException {
        final String acsFieldName = getFieldNameForACSQuery(profile, simpleSearchFilter.getField(), user);

        final String searchFilterTerm = simpleSearchFilter.getTerm();

        final String filterQuery = (simpleSearchFilter.isNot() ? NOT_TOKEN : EMPTY_STRING) + "(" + (isRangeQuery(searchFilterTerm) ? "" : TERM_EQUALS_TOKEN) + acsFieldName + SEARCH_IN_TERM_TOKEN + searchFilterTerm + ")";
        return filterQuery;
    }


    private String createComplexSearchFilterQuery(ComplexSearchFilter complexSearchFilter, String profile, User user) throws NoSuchProfileException {
        final StringBuilder complexFilterQuery = new StringBuilder();
        if (complexSearchFilter.isNot()) {
            complexFilterQuery.append(NOT_TOKEN);
        }

        complexFilterQuery.append("(");
        for (int i = 0; i < complexSearchFilter.getFilters().size(); i++) {
            final SearchFilter searchFilter = complexSearchFilter.getFilters().get(i);

            if (i != 0) {
                complexFilterQuery.append(complexSearchFilter.getConjunction());
            }

            final String searchFilterQuery = this.createSearchFilterQuery(searchFilter, profile, user);
            complexFilterQuery.append(searchFilterQuery);
        }
        complexFilterQuery.append(")");
        return complexFilterQuery.toString();
    }


    private String getFieldNameForACSQuery(String profile, String fieldName, User user) throws NoSuchProfileException {

        String strippedFieldName = removeMetadataPrefix(fieldName);

        strippedFieldName = removeElasticSearchSuffix(strippedFieldName);

        return getCSFieldName(profile, strippedFieldName, user);
    }

    private String removeMetadataPrefix(String fieldName) {
        if (fieldName.startsWith(METADATA_PREFIX)) {
            return fieldName.replace(METADATA_PREFIX, EMPTY_STRING);
        } else {
            return fieldName;
        }
    }

    private String removeElasticSearchSuffix(String fieldName) {
        if (fieldName.endsWith(RAW_SUFFIX)) {
            return fieldName.replace(RAW_SUFFIX, EMPTY_STRING);
        } else if (fieldName.endsWith(LOWERCASE_SUFFIX)) {
            return fieldName.replace(LOWERCASE_SUFFIX, EMPTY_STRING);
        } else {
            return fieldName;
        }
    }

    private String getCSFieldName(String profileId, String contentFieldName, User user) throws NoSuchProfileException {
        return profileFacade.getRepoFQDNFieldName(contentFieldName, profileId, user).replace("cmis:", "cm:");
    }
}