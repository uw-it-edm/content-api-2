package edu.uw.edm.contentapi2.service.util;

import com.google.common.base.Preconditions;

import java.util.Set;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ComplexSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.Order;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ProfiledSearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;

public class LegacySearchModelUtils {
    private static String COMPLEX_SEARCH_FILTER_DELIMITER = ";";
    private static String CASE_INSENSITIVE_EQUALS = "~=";
    private static String CASE_INSENSITIVE_NOT_EQUALS = "!~=";
    private static String EQUALS = "=";
    private static String GREATER_THAN_OR_EQUALS = "={gte}";
    private static String GREATER_THAN_OR_EQUALS_REGEX = "=\\{gte\\}";
    private static String GREATER_THAN = "={gt}";
    private static String GREATER_THAN_REGEX = "=\\{gt\\}";
    private static String LESS_THAN_OR_EQUALS = "={lte}";
    private static String LESS_THAN_OR_EQUALS_REGEX = "=\\{lte\\}";
    private static String LESS_THAN = "={lt}";
    private static String LESS_THAN_REGEX = "=\\{lt\\}";
    private static String NOT_EQUALS = "!=";

    public static ProfiledSearchQueryModel convertToSearchQueryModel(LegacySearchModel legacySearchModel) {
        Preconditions.checkNotNull(legacySearchModel, "LegacySearchModel required.");
        Preconditions.checkNotNull(legacySearchModel.getSearch(), "At least one search is required.");
        Preconditions.checkArgument((legacySearchModel.getSearch() != null) && (legacySearchModel.getSearch().size() >= 2), "Search must contain both '%s=' as well as at least one search additional search.", PROFILE_ID);

        final ProfiledSearchQueryModel searchQueryModel = new ProfiledSearchQueryModel();

        for (String search : legacySearchModel.getSearch()) {
            if (search.contains(PROFILE_ID)) {
                final String profileId = search.split(EQUALS)[1]; //PROFILE_ID=<profileId> is a required parameter
                searchQueryModel.setProfileId(profileId);
            } else if (search.contains(COMPLEX_SEARCH_FILTER_DELIMITER)) {
                final ComplexSearchFilter complexSearchFilter = createComplexSearchFilter(search, legacySearchModel);
                searchQueryModel.addFilter(complexSearchFilter);
            } else {
                final SimpleSearchFilter simpleSearchFilter = createSearchFilter(search);
                searchQueryModel.addFilter(simpleSearchFilter);
            }
        }

        searchQueryModel.setPageSize(legacySearchModel.getPageSize());
        searchQueryModel.setFrom(legacySearchModel.getPageStart());

        if (legacySearchModel.getOrderBy() != null) {
            final Order order = (legacySearchModel.getOrder() != null) ? Order.valueOf(legacySearchModel.getOrder().name()) : SearchOrder.getDefaultSearchOrder().getOrder();
            searchQueryModel.setSearchOrder(new SearchOrder(legacySearchModel.getOrderBy(), order));
        }

        return searchQueryModel;
    }

    private static ComplexSearchFilter createComplexSearchFilter(String search, LegacySearchModel legacySearchModel) {
        final String[] searchModelFilters = search.split(COMPLEX_SEARCH_FILTER_DELIMITER);
        final ComplexSearchFilter complexSearchFilter = new ComplexSearchFilter();
        if (legacySearchModel.getConjunction() != null) {
            complexSearchFilter.setConjunction(legacySearchModel.getConjunction());
        }
        for (String searchModelFilter : searchModelFilters) {
            final SimpleSearchFilter simpleSearchFilter = createSearchFilter(searchModelFilter);
            complexSearchFilter.addFilter(simpleSearchFilter);
        }
        return complexSearchFilter;
    }

    private static SimpleSearchFilter createSearchFilter(String searchModelFilter) {
        SimpleSearchFilter filter = null;

        if (searchModelFilter.contains(CASE_INSENSITIVE_NOT_EQUALS)) {
            //TODO: handle CASE_INSENSITIVE_NOT_EQUALS
        } else if (searchModelFilter.contains(CASE_INSENSITIVE_EQUALS)) {
            //TODO: handle  CASE_INSENSITIVE_EQUALS
        } else if (searchModelFilter.contains(GREATER_THAN_OR_EQUALS)) {
            filter = createSearchFilter(searchModelFilter, GREATER_THAN_OR_EQUALS, GREATER_THAN_OR_EQUALS_REGEX);
        } else if (searchModelFilter.contains(GREATER_THAN)) {
            filter = createSearchFilter(searchModelFilter, GREATER_THAN, GREATER_THAN_REGEX);
        } else if (searchModelFilter.contains(LESS_THAN_OR_EQUALS)) {
            filter = createSearchFilter(searchModelFilter, LESS_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS_REGEX);
        } else if (searchModelFilter.contains(LESS_THAN)) {
            filter = createSearchFilter(searchModelFilter, LESS_THAN, LESS_THAN_REGEX);
        } else if (searchModelFilter.contains(NOT_EQUALS)) {
            filter = createSearchFilter(searchModelFilter, NOT_EQUALS, NOT_EQUALS);
        } else if (searchModelFilter.contains(EQUALS)) {
            filter = createSearchFilter(searchModelFilter, EQUALS, EQUALS);
        } else {
            throw new IllegalArgumentException("Invalid search filter: '"+searchModelFilter+"'");
        }
        return filter;
    }


    private static SimpleSearchFilter createSearchFilter(String searchModelFilter, String operator, String operatorRegex) {
        final String[] searchTerms = searchModelFilter.split(operatorRegex);
        Preconditions.checkArgument((searchTerms.length == 2), "Invalid Search Model Filter:  '%s'", searchModelFilter);

        final String field = searchTerms[0];
        final String term = convertToLuceneTerm(searchTerms[1], operator);
        final boolean negate = ((operator == NOT_EQUALS) || (operator == CASE_INSENSITIVE_NOT_EQUALS));

        return new SimpleSearchFilter(field, term, negate);
    }

    private static String convertToLuceneTerm(String term, String operator) {
        if (operator == LESS_THAN) {
            term = "[* TO " + term + ">";
        }
        if (operator == LESS_THAN_OR_EQUALS) {
            term = "[* TO " + term + "]";
        }
        if (operator == GREATER_THAN) {
            term = "<" + term + " TO *]";
        }

        if (operator == GREATER_THAN_OR_EQUALS) {
            term = "[" + term + " TO *]";
        }
        return term;
    }


    public static DocumentSearchResults convertToDocumentSearchResults(SearchResultContainer searchResultContainer) {
        Preconditions.checkNotNull(searchResultContainer, "SearchResultContainer required.");

        final Set<ContentAPIDocument> documents = searchResultContainer.getSearchResults().stream().map(SearchResult::getDocument).collect(Collectors.toSet());
        final int totalCount = Math.toIntExact(searchResultContainer.getTotalCount());
        return DocumentSearchResults
                .builder()
                .documents(documents)
                .totalCount(totalCount)
                .build();
    }
}
