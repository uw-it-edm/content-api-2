package edu.uw.edm.contentapi2.service.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uw.edm.contentapi2.controller.content.v3.model.Conjunction;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentAPIDocument;
import edu.uw.edm.contentapi2.controller.content.v3.model.DocumentSearchResults;
import edu.uw.edm.contentapi2.controller.content.v3.model.LegacySearchModel;
import edu.uw.edm.contentapi2.controller.content.v3.model.SearchOrder;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ComplexSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.ProfiledSearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.query.SimpleSearchFilter;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResult;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;

import static edu.uw.edm.contentapi2.TestUtilities.getTestDocument;
import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.ContentAPI.PROFILE_ID;
import static org.junit.Assert.assertEquals;

public class LegacySearchModelUtilsTest {

    @Test
    public void convertToSearchQueryModelOrderingAndPaging() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();
        legacySearchModel.setOrder(SearchOrder.asc);
        legacySearchModel.setOrderBy("testOrderBy");
        legacySearchModel.setPageSize(12);
        legacySearchModel.setPageStart(2);

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId=123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", false));
        expectedSearchQueryModel.setSearchOrder(new edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchOrder("testOrderBy", "asc"));
        expectedSearchQueryModel.setPageSize(12);
        expectedSearchQueryModel.setFrom(2);
        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToSearchQueryModelHasNoSearchThrowIllegalArgumentException() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        legacySearchModel.setSearch(searches);
        LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToSearchQueryModelHasInvalidSearchThrowIllegalArgumentException() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId!123");
        legacySearchModel.setSearch(searches);

        LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
    }

    @Test
    public void convertToSearchQueryModelEquals() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId=123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelNotEquals() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId!=123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", true));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelGreater() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId={gt}123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "<123 TO *]", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelGreaterThanOrEquals() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId={gte}123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "[123 TO *]", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelLessThan() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId={lt}123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "[* TO 123>", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelLessThanOrEquals() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId={lte}123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "[* TO 123]", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelMultipleFilters() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId=123");
        searches.add("testField=1234");
        searches.add("testField2={lte}123");
        legacySearchModel.setSearch(searches);

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", false));
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testField", "1234", false));
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testField2", "[* TO 123]", false));

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelComplexFilters() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testId=123");
        searches.add("testField=1234;testField2={lte}123");
        legacySearchModel.setSearch(searches);

        final ComplexSearchFilter complexSearchFilter = new ComplexSearchFilter();
        complexSearchFilter.addFilter(new SimpleSearchFilter("testField", "1234", false));
        complexSearchFilter.addFilter(new SimpleSearchFilter("testField2", "[* TO 123]", false));

        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(new SimpleSearchFilter("testId", "123", false));
        expectedSearchQueryModel.addFilter(complexSearchFilter);

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToSearchQueryModelConjunction() {
        final LegacySearchModel legacySearchModel = new LegacySearchModel();

        final List<String> searches = new ArrayList<>();
        searches.add(PROFILE_ID + "=test-profile");
        searches.add("testField=1234;testField2={lte}123");
        legacySearchModel.setSearch(searches);
        legacySearchModel.setConjunction(Conjunction.or);

        final ComplexSearchFilter complexSearchFilter = new ComplexSearchFilter();
        complexSearchFilter.addFilter(new SimpleSearchFilter("testField", "1234", false));
        complexSearchFilter.addFilter(new SimpleSearchFilter("testField2", "[* TO 123]", false));
        complexSearchFilter.setConjunction(Conjunction.or);
        final ProfiledSearchQueryModel expectedSearchQueryModel = new ProfiledSearchQueryModel();
        expectedSearchQueryModel.setProfileId("test-profile");
        expectedSearchQueryModel.addFilter(complexSearchFilter);

        final SearchQueryModel result = LegacySearchModelUtils.convertToSearchQueryModel(legacySearchModel);
        assertEquals(expectedSearchQueryModel, result);
    }

    @Test
    public void convertToDocumentSearchResults() {
        final ContentAPIDocument document = getTestDocument("123");

        final SearchResult result1 = new SearchResult();
        result1.setDocument(document);

        final List<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(result1);

        final SearchResultContainer searchResultContainer = new SearchResultContainer();
        searchResultContainer.setTotalCount(2);
        searchResultContainer.setSearchResults(searchResults);

        final Set<ContentAPIDocument> documents = new HashSet<>();
        documents.add(document);

        final DocumentSearchResults expectedResult = DocumentSearchResults.builder()
                .totalCount(2)
                .documents(documents)
                .build();

        DocumentSearchResults result = LegacySearchModelUtils.convertToDocumentSearchResults(searchResultContainer);
        assertEquals(expectedResult.getTotalCount(), result.getTotalCount());
        assertEquals(expectedResult.getDocuments(), result.getDocuments());
        assertEquals(expectedResult.getResultListSize(), result.getResultListSize());
    }

    @Test
    public void convertToDocumentSearchResultsMultipleDocuments() {
        final ContentAPIDocument document1 = getTestDocument("123");
        final ContentAPIDocument document2 = getTestDocument("1234");
        final SearchResult result1 = new SearchResult();
        result1.setDocument(document1);
        final SearchResult result2 = new SearchResult();
        result2.setDocument(document2);

        final List<SearchResult> searchResults = new ArrayList<>();
        searchResults.add(result1);
        searchResults.add(result2);

        final SearchResultContainer searchResultContainer = new SearchResultContainer();
        searchResultContainer.setTotalCount(2);
        searchResultContainer.setSearchResults(searchResults);

        final Set<ContentAPIDocument> documents = new HashSet<>();
        documents.add(document1);
        documents.add(document2);

        final DocumentSearchResults expectedResult = DocumentSearchResults.builder()
                .totalCount(2)
                .documents(documents)
                .build();

        DocumentSearchResults result = LegacySearchModelUtils.convertToDocumentSearchResults(searchResultContainer);
        assertEquals(expectedResult.getTotalCount(), result.getTotalCount());
        assertEquals(expectedResult.getDocuments(), result.getDocuments());
        assertEquals(expectedResult.getResultListSize(), result.getResultListSize());
    }

    @Test
    public void convertToDocumentSearchResultsNoDocuments() {
        final SearchResultContainer searchResultContainer = new SearchResultContainer();
        searchResultContainer.setTotalCount(0);
        searchResultContainer.setSearchResults(new ArrayList<>());

        final DocumentSearchResults expectedResult = DocumentSearchResults.builder()
                .totalCount(0)
                .documents(new HashSet<>())
                .build();

        DocumentSearchResults result = LegacySearchModelUtils.convertToDocumentSearchResults(searchResultContainer);
        assertEquals(expectedResult.getTotalCount(), result.getTotalCount());
        assertEquals(expectedResult.getDocuments(), result.getDocuments());
        assertEquals(expectedResult.getResultListSize(), result.getResultListSize());
    }
}