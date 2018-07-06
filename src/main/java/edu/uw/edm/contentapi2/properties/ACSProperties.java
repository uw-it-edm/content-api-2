package edu.uw.edm.contentapi2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Maxime Deravet Date: 4/4/18
 */
@Component
@ConfigurationProperties(prefix = "uw.repository.acs")
@Data
public class ACSProperties {

    private String cmisUrl;
    private String openAPIUrl = "http://localhost:8081/alfresco";
    private String user;
    private String password;
    private String actAsHeader;

    private HttpLoggingInterceptor.Level httpClientLoggingLevel = HttpLoggingInterceptor.Level.NONE;

    private boolean autoUndoCheckout = true;


}
