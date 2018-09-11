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


        }

        public static final class AlfrescoAspects {
            public static final String TITLED = "P:cm:titled";
        }
    }

    public static final class CMIS {
        public static final String BASE_DOCUMENT_TYPE = "cmis:document";

        public static final String CREATED_BY_FQDN = "cmis:createdBy";
        public static final String CREATION_DATE_FQDN = "cmis:creationDate";
        public static final String CONTENT_STREAM_LENGTH_FQDN = "cmis:contentStreamLength";
        public static final String CONTENT_STREAM_MIME_TYPE_FQDN = "cmis:contentStreamMimeType";
        public static final String ITEM_ID_FQDN = "cmis:objectId";
        public static final String LAST_MODIFICATION_DATE_FQDN = "cmis:lastModificationDate";
        public static final String LAST_MODIFIER_FQDN = "cmis:lastModifier";
        public static final String NAME_FQDN = "cmis:name";


        public static final class Renditions {
            public static final String THUMBNAIL_KIND = "cmis:thumbnail";
            public static final class Filters {
                public static final String WEB = "application/pdf,image/*";
                public static final String NONE = "cmis:none";
            }


        }
    }
}
