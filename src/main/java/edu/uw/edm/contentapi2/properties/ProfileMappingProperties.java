package edu.uw.edm.contentapi2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "uw.content.mapping")
@Data
public class ProfileMappingProperties {
    private String profileMappingFilePath;
}
