package edu.uw.edm.contentapi2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Maxime Deravet
 * Date: 4/4/18
 */
@Component
@ConfigurationProperties(prefix = "uw.repository.acs")
@Data
public class ACSProperties {

    private String cmisUrl;
    private String user;
    private String password;
    private String actAsHeader;
}
