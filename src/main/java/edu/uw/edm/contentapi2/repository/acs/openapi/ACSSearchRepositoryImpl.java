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
import java.util.Optional;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchFacet;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.BucketResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.FacetResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.ExternalSearchDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.repository.exceptions.SearchRepositoryException;
import edu.uw.edm.contentapi2.security.User;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

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
        final QueryBody queryBody = buildQuery(profile, searchModel, user);

        final SearchResultContainer searchResultContainer = new SearchResultContainer();

        try {
            if (log.isTraceEnabled()) {
                log.trace(queryBody.toString());

            }
            Response<ResultSetRepresentation<ResultNodeRepresentation>> searchCall = acsSearchAPI.searchCall(queryBody).execute();

            if (!searchCall.isSuccessful()) {
                final String errorBody = searchCall.errorBody().string();
                throw new SearchRepositoryException("couldn't execute search  :" + errorBody, errorBody);
            }

            final ResultSetRepresentation<ResultNodeRepresentation> searchResult = searchCall.body();

            final List<SearchResult> results = new ArrayList<>();
            for (ResultNodeRepresentation resultNodeRepresentation : searchResult.getList()) {
                results.add(searchResultTransformer.toSearchResult(resultNodeRepresentation, profile, user));
            }

            final List<FacetResult> facets = createFacetResults(searchModel, searchResult);
            searchResultContainer.setFacets(facets);
            searchResultContainer.setTotalCount(searchResult.getPagination().getTotalItems());


            searchResultContainer.setSearchResults(results);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SearchRepositoryException(e);
        }


        return searchResultContainer;
    }

    private List<FacetResult> createFacetResults(SearchQueryModel searchModel, ResultSetRepresentation<ResultNodeRepresentation> searchResult) {
        final List<FacetResult> facetResults = new ArrayList<>();
        for (SearchFacet searchFacet : searchModel.getFacets()) { //create FacetResult for every facet in searchModel, even if empty
            final String facetLabel = searchFacet.getField();
            final Optional<ResultSetContextFacetFields> searchFacetResults = searchResult.getContext().getFacetFields()
                    .stream()
                    .filter(facetField -> facetField.getLabel().equals(facetLabel))
                    .findAny();
            facetResults.add(this.createFacetResult(facetLabel, searchFacetResults));
        }
        return facetResults;
    }

    private FacetResult createFacetResult(String facetLabel, Optional<ResultSetContextFacetFields> facetResults) {
        final FacetResult facetResult = new FacetResult(facetLabel);

        facetResults.ifPresent(facetFields ->
                facetFields.getBuckets().forEach(resultSetContextBuckets -> {
                    BucketResult bucketResult = new BucketResult();
                    bucketResult.setKey(resultSetContextBuckets.getLabel());
                    bucketResult.setCount(resultSetContextBuckets.getCount().longValue());

                    facetResult.addBucketResult(bucketResult);
                })
        );

        return facetResult;
    }


    private QueryBody buildQuery(String profile, SearchQueryModel searchModel, User user) throws NoSuchProfileException {
        QueryBody queryBody = new QueryBody();

        queryBody = searchQueryBuilder.addQuery(queryBody, searchModel);

        queryBody = searchQueryBuilder.addFilters(queryBody, searchModel.getFilters(), profile, user);

        queryBody = searchQueryBuilder.addContentModelFilter(profile, queryBody);

        queryBody = searchQueryBuilder.addPagination(queryBody, searchModel);

        queryBody = searchQueryBuilder.addSorting(queryBody, searchModel.getSearchOrder(), profile, user);

        queryBody = searchQueryBuilder.addDefaultIncludedInfo(queryBody);

        queryBody = searchQueryBuilder.addFacets(queryBody, searchModel.getFacets(), profile, user);

        return queryBody;
    }


}
