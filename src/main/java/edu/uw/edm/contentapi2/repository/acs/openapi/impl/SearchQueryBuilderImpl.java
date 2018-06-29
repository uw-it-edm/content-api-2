package edu.uw.edm.contentapi2.repository.acs.openapi.impl;

import com.google.common.base.Strings;

import com.alfresco.client.api.search.body.QueryBody;
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

import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
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
            queryBody.query(new RequestQuery().query("name:*"));
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
            requestSortDefinition.setField(getACSFieldName(profile, searchOrder.getTerm(), user));
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

    private RequestFilterQuery toRequestFilterQuery(SearchFilter searchFilter, String profile, User user) throws NoSuchProfileException {
        String acsFieldName = getACSFieldName(profile, searchFilter.getField(), user);

        //TODO this is ugly
        String filterQuery = (searchFilter.isNot() ? NOT_TOKEN : "") + "(" + TERM_EQUALS_TOKEN + acsFieldName + SEARCH_IN_TERM_TOKEN + searchFilter.getTerm() + ")";

        return new RequestFilterQuery().query(filterQuery);
    }

    private String getACSFieldName(String profileId, String contentFieldName, User user) throws NoSuchProfileException {
        return profileFacade.getRepoFQDNFieldName(contentFieldName, profileId, user);
    }
}
