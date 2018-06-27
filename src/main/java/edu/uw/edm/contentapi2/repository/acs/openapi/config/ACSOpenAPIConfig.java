package edu.uw.edm.contentapi2.repository.acs.openapi.config;

import com.alfresco.client.AbstractClient;
import com.alfresco.client.AlfrescoClient;
import com.alfresco.client.api.search.SearchAPI;
import com.alfresco.client.utils.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import edu.uw.edm.contentapi2.properties.ACSProperties;
import edu.uw.edm.contentapi2.security.User;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Maxime Deravet Date: 6/25/18
 */

@Configuration
public class ACSOpenAPIConfig {


    @Bean
    public AlfrescoClient alfrescoClient(ACSProperties acsProperties) {

        //TODO add header interceptor for act as
        AbstractClient.Builder<AlfrescoClient> alfrescoClientBuilder = new AlfrescoClient.Builder()
                .connect(acsProperties.getOpenAPIUrl(), acsProperties.getUser(), acsProperties.getPassword());


        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        ArrayList<Protocol> protocols = new ArrayList<>(1);
        protocols.add(Protocol.HTTP_1_1);
        builder.protocols(protocols);
        builder.connectTimeout(10, TimeUnit.SECONDS);

        Interceptor authenticationInterceptor;
        authenticationInterceptor = new BasicAuthInterceptor(acsProperties.getUser(), acsProperties.getPassword());

        builder.addInterceptor(authenticationInterceptor);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();


        //TODO make log level configurable
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        builder.addInterceptor(logging);

        builder.addInterceptor(new ActAsHeaderInterceptor(acsProperties.getActAsHeader()));

        OkHttpClient okHttpClient = builder.build();


        alfrescoClientBuilder.okHttpClient(okHttpClient);

        return alfrescoClientBuilder.build();

    }


    @Bean
    public SearchAPI searchAPI(AlfrescoClient client) {
        return client.getSearchAPI();
    }

    protected final static class ActAsHeaderInterceptor implements Interceptor {
        private String actAsHeader;

        ActAsHeaderInterceptor(String actAsHeader) {
            this.actAsHeader = actAsHeader;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (user != null) {
                Request newRequest;
                newRequest = request.newBuilder()
                        .addHeader(actAsHeader, user.getUsername())
                        .build();
                return chain.proceed(newRequest);
            } else {
                return chain.proceed(request);
            }

        }

    }

    protected final static class BasicAuthInterceptor implements Interceptor {

        String auth;

        BasicAuthInterceptor(String username, String password) {
            String credentials = username + ":" + password;
            auth = "Basic " + Base64.encodeBytes(credentials.getBytes());
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request newRequest = chain.request().newBuilder().addHeader("Authorization", auth).build();
            return chain.proceed(newRequest);
        }
    }
}
