package edu.uw.edm.contentapi2.service.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestUtils {
    private static final String DOWNLOAD_URL_TEMPLATE = "%s%s/v3/file/%s?rendition=Primary";

    public static String getOriginalFileDownloadURL(String itemId, HttpServletRequest request) {
        Preconditions.checkArgument(!StringUtils.isEmpty(itemId), "ItemId is required.");
        Preconditions.checkNotNull(request, "Request is required");

        UriComponents currentUriComponents = ServletUriComponentsBuilder.fromRequest(request).build();

        final String domain = getSchemeAndDomain(currentUriComponents);
        final String basePath = getBasePath(currentUriComponents);
        final String format = String.format(DOWNLOAD_URL_TEMPLATE, domain, basePath, itemId);
        log.debug("download url for '{}'  is : {}", itemId, format);
        //uriComponents.getSchemeSpecificPart()
        return format;
    }

    private static String getBasePath( UriComponents uriComponents){
        /*  /content or /content/sso */
        final String basePath = uriComponents.getPath().split("/v")[0];
        return basePath;
    }
    private static String getSchemeAndDomain(UriComponents uriComponents) {
        return uriComponents.getScheme() + "://" + uriComponents.getHost() + ((uriComponents.getPort() == -1 ) ? "" : (":" + uriComponents.getPort()));
    }

}
