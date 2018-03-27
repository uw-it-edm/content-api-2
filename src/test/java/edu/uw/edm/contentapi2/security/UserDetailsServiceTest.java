package edu.uw.edm.contentapi2.security;

import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

/**
 * @author Maxime Deravet
 * Date: 3/27/18
 */

public class UserDetailsServiceTest {

    UserDetailsService userDetailsService;


    @Test
    public void userShouldBeLoadedByNameTest() {
        userDetailsService = new UserDetailsService();

        UserDetails userDetails = userDetailsService.loadUserByUsername("test-user");

        assertThat(userDetails.getUsername(), is(equalTo("test-user")));
        assertThat(userDetails.getPassword(), isEmptyString());
    }
}