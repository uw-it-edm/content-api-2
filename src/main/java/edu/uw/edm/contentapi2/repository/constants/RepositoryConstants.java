package edu.uw.edm.contentapi2.repository.constants;

/**
 * @author Maxime Deravet Date: 5/17/18
 */
public class RepositoryConstants {


    public static final class ContentAPI {
        public static final String PROFILE_ID = "ProfileId";
        public static final String ID = "id";
        public static final String LABEL = "label";
    }

    public static final class Alfresco {
        public static final class ExtensionNames {
            public static final String MANDATORY_ASPECTS = "mandatoryAspects";
        }

        public static final class AlfrescoFields {

            public static final String TITLE_FQDN = "cm:title";
            public static final String ITEM_ID_FQDN = "cmis:objectId";

        }

        public static final class AlfrescoAspects {
            public static final String TITLED = "P:cm:titled";
        }
    }

    public static final class CMIS {
        public static final String BASE_DOCUMENT_TYPE = "cmis:document";
    }
}
