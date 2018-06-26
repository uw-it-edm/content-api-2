package edu.uw.edm.contentapi2.common.impl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.common.model.ProfileMapping;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;

@Service
public class YamlFieldMapper implements FieldMapper {
    private final String MAPPING_FILE_PATH = "profile-mapping.yml";

    private Map<String, ProfileMapping> profileMappings = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        List<ProfileMapping> profileMappings = loadProfileMappings(MAPPING_FILE_PATH);
        for (ProfileMapping profileMapping : profileMappings) {
            this.profileMappings.put(profileMapping.getProfileName(), profileMapping);
        }
    }

    private List<ProfileMapping> loadProfileMappings(String mappingFilePath) throws IOException {
        InputStream inputStream = new ClassPathResource(mappingFilePath).getInputStream();
        Yaml yaml = new Yaml();

        return (List<ProfileMapping>) yaml.load(inputStream);

    }

    @Override
    public String getContentTypeForProfile(String profileId) throws NoSuchProfileException {
        final ProfileMapping profile = profileMappings.get(profileId);
        final String contentType = profile != null ? profile.getContentType() : null;
        if (contentType == null) {
            throw new NoSuchProfileException("Unable to map '" + profileId + "' to a content-type.");
        }

        return contentType;
    }

    @Override
    public String convertToContentApiFieldFromRepositoryField(String profile, String repoFieldLocalName) {
        ProfileMapping profileMapping = profileMappings.get(profile);

        String contentApiFieldName = profileMapping.getFieldByRepoName(repoFieldLocalName);
        if (contentApiFieldName == null) {
            contentApiFieldName = repoFieldLocalName;
        }
        return contentApiFieldName;
    }
}
