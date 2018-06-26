package edu.uw.edm.contentapi2.controller.v3.model;

import com.google.common.base.Strings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;
import static edu.uw.edm.contentapi2.repository.constants.Constants.ContentAPI.PROFILE_ID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString
public class ContentAPIDocument {
    public static final String METADATA_KEY = "metadata";
    public static final String ID_KEY = "id";
    public static final String LABEL_KEY = "label";

    private String id;
    private String label;
    private Map<String, Object> metadata = new HashMap<>();

    public ContentAPIDocument() {
    }

    @SuppressWarnings("unchecked")
    public ContentAPIDocument(Map data) {
        if (data.containsKey(ID_KEY) && data.get(ID_KEY) != null) {
            this.id = data.get(ID_KEY).toString();
        }
        if (data.containsKey(LABEL_KEY) && data.get(LABEL_KEY) != null) {

            this.label = data.get(LABEL_KEY).toString();
        }
        if (data.containsKey(METADATA_KEY)) {
            this.metadata = (Map<String, Object>) data.get(METADATA_KEY);
        }
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(ID_KEY, id);
        map.put(LABEL_KEY, label);
        map.put(METADATA_KEY, metadata);

        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Transient
    public String getProfileId() {
        final String profileId = (String) this.getMetadata().get(PROFILE_ID);
        checkArgument(!Strings.isNullOrEmpty(profileId), "Profile is a required metadata field");
        return profileId;
    }
}
