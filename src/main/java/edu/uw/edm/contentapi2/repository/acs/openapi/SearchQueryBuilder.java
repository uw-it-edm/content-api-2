package edu.uw.edm.contentapi2.repository.acs.openapi;

import com.alfresco.client.api.search.body.QueryBody;

import java.util.List;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 6/25/18
 */
public interface SearchQueryBuilder {
    QueryBody addQuery(QueryBody queryBody, SearchQueryModel searchModel);

    QueryBody addSorting(QueryBody queryBody, SearchOrder searchOrder, String profile, User user);

    QueryBody addPagination(QueryBody queryBody, SearchQueryModel searchModel);

    QueryBody addFilters(QueryBody queryBody, List<SearchFilter> filters, String profile, User user);

    QueryBody addSiteFilter(String profile, QueryBody queryBody);

    QueryBody addDefaultIncludedInfo(QueryBody queryBody);
}
