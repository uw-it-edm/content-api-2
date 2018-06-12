package edu.uw.edm.contentapi2.service.impl;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.properties.ProfileProperties;
import edu.uw.edm.contentapi2.repository.ExternalDocumentRepository;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ProfileDefinitionServiceImplTest {
    ProfileDefinitionServiceImpl profileDefinitionService;
    @Mock
    ExternalDocumentRepository documentRepository;
    @Mock
    User user;

    @Before
    public void setup() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("testProfile", "D:test:TestProfile");

        ProfileProperties profileProperties = new ProfileProperties();
        profileProperties.setMappings(mapping);
        this.profileDefinitionService = new ProfileDefinitionServiceImpl(documentRepository, profileProperties);
    }

    @Test
    public void getProfileDefinition() throws NoSuchProfileException {
        Map<String, PropertyDefinition<?>> acsMetadata = new HashMap<>();
        ProfileDefinitionV4 expectedResult = new ProfileDefinitionV4("testProfile", acsMetadata);
        ProfileDefinitionV4 result = this.profileDefinitionService.getProfileDefinition("testProfile", user);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchProfileException.class)
    public void whenProfileIsNotDefinedThrowNoSuchProfileException() throws NoSuchProfileException {
        this.profileDefinitionService.getProfileDefinition("invalidProfile", user);
    }

}
