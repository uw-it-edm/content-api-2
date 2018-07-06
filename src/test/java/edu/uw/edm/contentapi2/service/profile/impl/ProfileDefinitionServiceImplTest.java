package edu.uw.edm.contentapi2.service.profile.impl;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.ExternalProfileRepository;
import edu.uw.edm.contentapi2.repository.constants.RepositoryConstants;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileDefinitionServiceImplTest {
    ProfileDefinitionServiceImpl profileDefinitionService;
    @Mock
    ExternalProfileRepository profileRepository;
    @Mock
    FieldMapper fieldMapper;
    @Mock
    User user;

    @Before
    public void setup() {
        final PropertyDefinition<?> idDefinition = createMockPropertyDefinition("test:testId", "testId", PropertyType.ID);
        final PropertyDefinition<?> labelDefinition = createMockPropertyDefinition("test:testLabel", "testLabel", PropertyType.STRING);
        final PropertyDefinition<?> testMetadataDefinition = createMockPropertyDefinition("test:testField", "testField", PropertyType.STRING);
        final PropertyDefinition<?> test2MetadataDefinition = createMockPropertyDefinition("test:testField2", "testField2", PropertyType.STRING);


        Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<>();
        propertyDefinitions.put(RepositoryConstants.Alfresco.AlfrescoFields.ITEM_ID_FQDN, idDefinition);
        propertyDefinitions.put(RepositoryConstants.Alfresco.AlfrescoFields.LABEL_FQDN, labelDefinition);
        propertyDefinitions.put("test:testField", testMetadataDefinition);
        when(profileRepository.getPropertyDefinition(any(User.class), eq("D:test:TestProfile"))).thenReturn(propertyDefinitions);


        Map<String, PropertyDefinition<?>> propertyDefinition2s = new HashMap<>();
        propertyDefinition2s.put(RepositoryConstants.Alfresco.AlfrescoFields.ITEM_ID_FQDN, idDefinition);
        propertyDefinition2s.put(RepositoryConstants.Alfresco.AlfrescoFields.LABEL_FQDN, labelDefinition);
        propertyDefinition2s.put("test:testField", testMetadataDefinition);
        propertyDefinition2s.put("test:testField2", test2MetadataDefinition);
        when(profileRepository.getPropertyDefinition(any(User.class), eq("D:test:TestProfile2"))).thenReturn(propertyDefinition2s);

        this.profileDefinitionService = new ProfileDefinitionServiceImpl(profileRepository, fieldMapper);
    }


    private PropertyDefinition createMockPropertyDefinition(String id, String localName, PropertyType propertyType) {
        final PropertyDefinition<?> mockDefinition = mock(PropertyDefinition.class);
        when(mockDefinition.getId()).thenReturn(id);
        when(mockDefinition.getLocalName()).thenReturn(localName);
        when(mockDefinition.getPropertyType()).thenReturn(propertyType);
        return mockDefinition;

    }

    private FieldDefinition createTestFieldDefinition(String repoFieldName) {
        return FieldDefinition.builder()
                .repoFieldName(repoFieldName)
                .type(MappingType.string)
                .validator("none")//TODO: implement validators
                .build();
    }

    @Test
    public void getProfileDefinitionAppliesProfileSpecificFieldMapping() throws NoSuchProfileException {
        final String PROFILE_WITH_OVERRIDE = "testProfile";
        when(fieldMapper.getContentTypeForProfile(eq(PROFILE_WITH_OVERRIDE))).thenReturn("D:test:TestProfile");
        when(fieldMapper.convertToContentApiFieldFromRepositoryField(eq(PROFILE_WITH_OVERRIDE), eq("testField"))).thenReturn("TestOverride");
        //when(fieldMapper.convertToContentApiFieldFromRepositoryField(eq(PROFILE_WITH_OVERRIDE), eq("testField2"))).thenReturn("testField2");

        final FieldDefinition testField = createTestFieldDefinition("test:testField");
        final FieldDefinition id = createTestFieldDefinition("test:testId");
        final FieldDefinition label = createTestFieldDefinition("test:testLabel");


        Map<String, FieldDefinition> acsMetadata = new HashMap<>();
        acsMetadata.put("TestOverride", testField);

        ProfileDefinitionV4 expectedResult = ProfileDefinitionV4.builder()
                .profile(PROFILE_WITH_OVERRIDE)
                .id(id)
                .label(label)
                .metadata(acsMetadata)
                .build();

        ProfileDefinitionV4 result = this.profileDefinitionService.getProfileDefinition(PROFILE_WITH_OVERRIDE, user);

        assertEquals(expectedResult.getId().getRepoFieldName(), result.getId().getRepoFieldName());
        assertEquals(expectedResult.getLabel().getRepoFieldName(), result.getLabel().getRepoFieldName());
        assertEquals(expectedResult.getProfile(), result.getProfile());
        assertEquals(expectedResult.getMetadata().size(), result.getMetadata().size());
        assertEquals(expectedResult.getMetadata().keySet(), result.getMetadata().keySet());
    }

    @Test
    public void getProfileDefinitionWithoutFieldMapping() throws NoSuchProfileException {
        final String PROFILE_WITHOUT_OVERRIDE = "testProfile2";
        when(fieldMapper.getContentTypeForProfile(eq(PROFILE_WITHOUT_OVERRIDE))).thenReturn("D:test:TestProfile2");
        when(fieldMapper.convertToContentApiFieldFromRepositoryField(anyString(), anyString())).thenAnswer(i -> i.getArguments()[1]);//return second argument
        final FieldDefinition testField = createTestFieldDefinition("test:testField");
        final FieldDefinition testField2 = createTestFieldDefinition("test:testField2");
        final FieldDefinition id = createTestFieldDefinition("test:testId");
        final FieldDefinition label = createTestFieldDefinition("test:testLabel");

        final Map<String, FieldDefinition> acsMetadata = new HashMap<>();
        acsMetadata.put("testField", testField);
        acsMetadata.put("testField2", testField2);

        final ProfileDefinitionV4 expectedResult = ProfileDefinitionV4.builder()
                .profile(PROFILE_WITHOUT_OVERRIDE)
                .id(id)
                .label(label)
                .metadata(acsMetadata)
                .build();

        final ProfileDefinitionV4 result = this.profileDefinitionService.getProfileDefinition(PROFILE_WITHOUT_OVERRIDE, user);

        assertEquals(expectedResult.getId().getRepoFieldName(), result.getId().getRepoFieldName());
        assertEquals(expectedResult.getLabel().getRepoFieldName(), result.getLabel().getRepoFieldName());
        assertEquals(expectedResult.getProfile(), result.getProfile());
        assertEquals(expectedResult.getMetadata().size(), result.getMetadata().size());
        assertEquals(expectedResult.getMetadata().keySet(), result.getMetadata().keySet());
    }

    @Test(expected = NoSuchProfileException.class)
    public void whenProfileIsNotDefinedThrowNoSuchProfileException() throws NoSuchProfileException {
        when(fieldMapper.getContentTypeForProfile(eq("invalidProfile"))).thenThrow(NoSuchProfileException.class);
        this.profileDefinitionService.getProfileDefinition("invalidProfile", user);
    }

}
