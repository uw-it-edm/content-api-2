package edu.uw.edm.contentapi2.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * @author Maxime Deravet Date: 2019-01-04
 */
@Configuration
public class FilterConfiguration {
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean() {
        FilterRegistrationBean<ForwardedHeaderFilter> registrationBean = new FilterRegistrationBean();
        ForwardedHeaderFilter customURLFilter = new ForwardedHeaderFilter();

        registrationBean.setFilter(customURLFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); //set precedence
        return registrationBean;
    }

}
