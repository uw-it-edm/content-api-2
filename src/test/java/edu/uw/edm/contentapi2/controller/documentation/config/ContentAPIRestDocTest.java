package edu.uw.edm.contentapi2.controller.documentation.config;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.OverridesAttribute;

import edu.uw.edm.contentapi2.properties.SecurityProperties;
import edu.uw.edm.contentapi2.security.UserDetailsService;

/**
 * @author Maxime Deravet Date: 7/13/18
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@WebMvcTest()
@AutoConfigureRestDocs(
        uriHost = "server.dns",
        uriScheme = "https",
        uriPort = 443)
@Import(ResultHandlerConfiguration.class)
public @interface ContentAPIRestDocTest {

    String GENERATED_SNIPPETS_BASE_PATH = "build/generated-snippets";

    @OverridesAttribute(constraint = WebMvcTest.class, name = "controllers")
    Class<?>[] controllers();

    @OverridesAttribute(constraint = AutoConfigureRestDocs.class, name = "outputDir")
    String outputDir() default GENERATED_SNIPPETS_BASE_PATH;

    @OverridesAttribute(constraint = WebMvcTest.class, name = "includeFilters")
    ComponentScan.Filter[] includeFilters() default {
            @ComponentScan.Filter(classes = EnableWebSecurity.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityProperties.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserDetailsService.class)
    };

}
