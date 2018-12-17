package edu.uw.edm.contentapi2.controller.content.v3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.controller.content.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.repository.exceptions.RepositoryException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.FileServingService;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/content/v3/file")
public class FileV3Controller {
    private FileServingService fileServingService;

    @Autowired
    public FileV3Controller(FileServingService fileServingService) {
        this.fileServingService = fileServingService;
    }

    @Deprecated
    @RequestMapping(value = "{itemId}/{name:.*}", method = RequestMethod.GET)
    public void read(@PathVariable("itemId") String itemId,
                     @PathVariable("name") String name,
                     @RequestParam(value = "disposition", defaultValue = "inline") ContentDispositionType contentDispositionType,
                     @AuthenticationPrincipal User user,
                     HttpServletRequest request,
                     HttpServletResponse response) throws RepositoryException, IOException {
        ContentRenditionType renditionType = ContentRenditionType.Primary;
        if(StringUtils.isBlank(name) || name.toUpperCase().endsWith(".PDF") ){
            renditionType = ContentRenditionType.Web;
        }
        read(itemId, renditionType, null, false, contentDispositionType, user, request, response);
    }


    @RequestMapping(value = "{itemId}", method = RequestMethod.GET)
    public void read(
            @PathVariable("itemId") String itemId,
            @RequestParam(value = "rendition", required = false, defaultValue = "Web") ContentRenditionType renditionType,
            @Deprecated
            @RequestParam(value = "forcePDF", required = false) Boolean forcePDF, // Deprecated feature that would do on the fly pdf conversion
            @RequestParam(value = "useChannel", required = false, defaultValue = "false") boolean useChannel,
            @RequestParam(value = "disposition", defaultValue = "inline") ContentDispositionType contentDispositionType,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            HttpServletResponse response) throws RepositoryException, IOException {

        if (forcePDF != null) {
            Metrics.counter("edm.repo.forcePdf").increment();
            log.warn("Deprecated parameter 'forcePDF' has been passed from client, for itemId '{}' and user '{}'", itemId, user.getUsername());
        }
        fileServingService.serveFile(itemId, renditionType, contentDispositionType, useChannel, user, request, response);
    }


}
