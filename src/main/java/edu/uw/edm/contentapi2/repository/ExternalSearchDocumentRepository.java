package edu.uw.edm.contentapi2.repository;

import edu.uw.edm.contentapi2.controller.search.v1.model.query.SearchQueryModel;
import edu.uw.edm.contentapi2.controller.search.v1.model.result.SearchResultContainer;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;

/**
 * @author Maxime Deravet Date: 4/3/18
 */
public interface ExternalSearchDocumentRepository {

    SearchResultContainer searchDocuments(String profile, SearchQueryModel searchModel, User user) throws RepositoryException;
}
