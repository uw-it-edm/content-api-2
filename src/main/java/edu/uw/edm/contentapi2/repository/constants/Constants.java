package edu.uw.edm.contentapi2.repository.constants;

/**
 * @author Maxime Deravet Date: 5/17/18
 */
public class Constants {


    public static final class ContentAPI {
        public static final String PROFILE_ID = "ProfileId";
    }

    public static final class Alfresco {
        public static final class AlfrescoFields {

            public static final String TITLE_FQDN = "cm:title";

        }

        public static final class AlfrescoAspects {
            public static final String TITLED = "P:cm:titled";
        }
    }

    public static final class CMIS {
        public static final String BASE_DOCUMENT_TYPE = "cmis:document";
    }
}
