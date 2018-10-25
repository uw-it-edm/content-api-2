package edu.uw.edm.contentapi2.repository.acs.openapi;

import com.alfresco.client.api.search.body.QueryBody;

import java.util.List;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFacet;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
public interface SearchQueryBuilder {
    QueryBody addQuery(QueryBody queryBody, SearchQueryModel searchModel);

    QueryBody addSorting(QueryBody queryBody, SearchOrder searchOrder, String profile, User user) throws NoSuchProfileException;

    QueryBody addPagination(QueryBody queryBody, SearchQueryModel searchModel);

    QueryBody addSiteFilter(String profile, QueryBody queryBody);

    QueryBody addDefaultIncludedInfo(QueryBody queryBody);

    QueryBody addIsDocumentFilter(String profile, QueryBody queryBody);

    QueryBody addFilters(QueryBody queryBody, List<SearchFilter> searchFilters, String profileId, User user) throws NoSuchProfileException;

    QueryBody addFacets(QueryBody queryBody, List<SearchFacet> facets, String profile, User user) throws NoSuchProfileException;
}
