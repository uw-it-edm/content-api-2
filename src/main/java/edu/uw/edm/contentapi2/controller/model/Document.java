package edu.uw.edm.contentapi2.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString
public class Document {
    private String id;
    private String label;
    private Map<String, Object> metadata = new HashMap<>();

    public Document() {
    }

    @SuppressWarnings("unchecked")
    public Document(Map data) {
        if (data.containsKey("id") && data.get("id") != null) {
            this.id = data.get("id").toString();
        }
        if (data.containsKey("label") && data.get("label") != null) {

            this.label = data.get("label").toString();
        }
        if (data.containsKey("metadata")) {
            this.metadata = (Map<String, Object>) data.get("metadata");
        }
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("label", label);
        map.put("metadata", metadata);

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
}
