package edu.uw.edm.contentapi2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "uw.profile")
@Data
public class ProfileProperties {
    private Map<String, String> mappings;

    public String getContentTypeForProfile(String profileId) throws NoSuchProfileException {
        String contentType = this.getMappings() != null ? this.getMappings().get(profileId): null;
        if(contentType == null){
            throw new NoSuchProfileException("Unable to map '" + profileId + "' to a content-type.");
        }
        return contentType;
    }
}
