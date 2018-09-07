package edu.uw.edm.contentapi2.service.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestUtils {
    private static final String DOWNLOAD_URL_TEMPLATE = "%s%s/v3/file/%s?rendition=Primary";
    private static final String X_FORWARDED_HOST_HEADER = "X-FORWARDED-HOST";

    public static String getOriginalFileDownloadURL(String itemId, HttpServletRequest request) {
        Preconditions.checkArgument(!StringUtils.isEmpty(itemId), "ItemId is required.");
        Preconditions.checkNotNull(request, "Request is required");

        final String domain = getSchemeAndDomain(request);
        final String basePath = getBasePath(request);
        final String format = String.format(DOWNLOAD_URL_TEMPLATE, domain, basePath, itemId);
        log.debug("download url for '{}'  is : {}", itemId, format);

        return format;
    }

    private static String getBasePath( HttpServletRequest request){
        /*  /content or /content/sso */
        final String basePath = request.getRequestURI().split("/v")[0];
        return basePath;
    }
    private static String getSchemeAndDomain(HttpServletRequest request) {
        return request.getScheme() + "://" + getServerName(request) + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : (":" + request.getServerPort()));
    }

    private static String getServerName(HttpServletRequest request) {
        // X_FORWARDED_HOST_HEADER could contain multiple domains if routed through multiple Proxy.
        // The first host should be the original requested host
        final String hostFromHeader = request.getHeader(X_FORWARDED_HOST_HEADER);
        if (!StringUtils.isEmpty(hostFromHeader)) {
            return Iterables.getFirst(Splitter.on(',').split(hostFromHeader), hostFromHeader);
        } else {
            return request.getServerName();
        }
    }
}
