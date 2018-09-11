package edu.uw.edm.contentapi2.repository.acs.openapi;

import com.alfresco.client.api.search.SearchAPI;
import com.alfresco.client.api.search.body.QueryBody;
import com.alfresco.client.api.search.model.ResultNodeRepresentation;
import com.alfresco.client.api.search.model.ResultSetContextFacetFields;
import com.alfresco.client.api.search.model.ResultSetRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.BucketResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.FacetResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 6/22/18
 */
@Slf4j
@Service
public class ACSSearchRepositoryImpl implements ExternalSearchDocumentRepository {


    private SearchAPI acsSearchAPI;
    private SearchQueryBuilder searchQueryBuilder;
    private SearchResultTransformer searchResultTransformer;


    @Autowired
    public ACSSearchRepositoryImpl(SearchAPI acsSearchAPI, SearchQueryBuilder searchQueryBuilder, SearchResultTransformer searchResultTransformer) {
        this.acsSearchAPI = acsSearchAPI;
        this.searchQueryBuilder = searchQueryBuilder;
        this.searchResultTransformer = searchResultTransformer;
    }


    @Override
    public SearchResultContainer searchDocuments(String profile, SearchQueryModel searchModel, User user) throws RepositoryException {
        QueryBody queryBody = buildQuery(profile, searchModel, user);

        SearchResultContainer searchResultContainer = new SearchResultContainer();


        try {
            if (log.isTraceEnabled()) {
                log.trace(queryBody.toString());

            }
            ResultSetRepresentation<ResultNodeRepresentation> searchResult = acsSearchAPI.searchCall(queryBody).execute().body();
            if (searchResult == null) {
                throw new RepositoryException("couldn't execute search");
            }

            final List<SearchResult> results = new ArrayList<>();
            for (ResultNodeRepresentation resultNodeRepresentation : searchResult.getList()) {
                results.add(searchResultTransformer.toSearchResult(resultNodeRepresentation, profile, user));
            }

            List<FacetResult> facets = searchResult.getContext()
                    .getFacetFields()
                    .stream()
                    .map(this::createFacetResult)
                    .collect(Collectors.toList());

            searchResultContainer.setFacets(facets);

            searchResultContainer.setTotalCount(searchResult.getPagination().getTotalItems());


            searchResultContainer.setSearchResults(results);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RepositoryException(e);
        }


        return searchResultContainer;
    }

    private FacetResult createFacetResult(ResultSetContextFacetFields resultSetContextFacetFields) {
        FacetResult facetResult = new FacetResult(resultSetContextFacetFields.getLabel());

        resultSetContextFacetFields.getBuckets().forEach(resultSetContextBuckets -> {
            BucketResult bucketResult = new BucketResult();
            bucketResult.setKey(resultSetContextBuckets.getLabel());
            bucketResult.setCount(resultSetContextBuckets.getCount().longValue());

            facetResult.addBucketResult(bucketResult);
        });

        return facetResult;
    }


    private QueryBody buildQuery(String profile, SearchQueryModel searchModel, User user) throws NoSuchProfileException {
        QueryBody queryBody = new QueryBody();

        queryBody = searchQueryBuilder.addQuery(queryBody, searchModel);

        queryBody = searchQueryBuilder.addFilters(queryBody, searchModel.getFilters(), profile, user);

        queryBody = searchQueryBuilder.addSiteFilter(profile, queryBody);

        queryBody = searchQueryBuilder.addIsDocumentFilter(profile, queryBody);

        queryBody = searchQueryBuilder.addPagination(queryBody, searchModel);

        queryBody = searchQueryBuilder.addSorting(queryBody, searchModel.getSearchOrder(), profile, user);

        queryBody = searchQueryBuilder.addDefaultIncludedInfo(queryBody);

        queryBody = searchQueryBuilder.addFacets(queryBody, searchModel.getFacets(), profile, user);


        return queryBody;
    }


}
