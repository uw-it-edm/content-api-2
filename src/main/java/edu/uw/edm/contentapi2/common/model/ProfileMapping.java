package edu.uw.edm.contentapi2.common.model;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProfileMapping {
    private String profileName;
    private String contentType;
    private Map<String, String> contentApiToRepoFieldMapping;
    private Map<String, String> repoToContentApiFieldMapping;

    public String getFieldByContentApiName(String contentApiName){
        return contentApiToRepoFieldMapping.get(contentApiName);
    }

    public String getFieldByRepoName(String repoFieldLocalName) {
        if(repoToContentApiFieldMapping == null){
            initRepoToContentApiFieldMapping();
        }
        return repoToContentApiFieldMapping.get(repoFieldLocalName);
    }
    private void initRepoToContentApiFieldMapping(){
        final Map<String, String> invertedMap = contentApiToRepoFieldMapping.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        repoToContentApiFieldMapping = invertedMap;
    }
}
