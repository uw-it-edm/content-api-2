package edu.uw.edm.contentapi2.common.model;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Maxime Deravet Date: 7/5/18
 */
public class ProfileMappingTest {


    @Test
    public void whenDuplicateRepoFieldThenLastHasPrecedenceTest() {
        ProfileMapping profileMapping = new ProfileMapping();

        profileMapping.setProfileName("my-profile");
        LinkedHashMap<String, String> contentApiToRepoFieldMapping = new LinkedHashMap<>();

        contentApiToRepoFieldMapping.put("ContentField", "repoField");
        contentApiToRepoFieldMapping.put("OverwrittenContentField", "repoField");

        profileMapping.setContentApiToRepoFieldMapping(contentApiToRepoFieldMapping);


        assertThat(profileMapping.getFieldByRepoName("repoField"), is("OverwrittenContentField"));
    }
}