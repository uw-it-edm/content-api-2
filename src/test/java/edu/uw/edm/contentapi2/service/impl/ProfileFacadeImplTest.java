package edu.uw.edm.contentapi2.service.impl;

import com.google.gson.internal.LinkedTreeMap;

import com.alfresco.client.api.search.model.ResultNodeRepresentation;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Property;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.exceptions.UndefinedFieldException;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.FieldConversionService;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;

import static edu.uw.edm.contentapi2.repository.constants.RepositoryConstants.Alfresco.ALFRESCO_SYSTEM_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileFacadeImplTest {
    @Mock
    private ProfileDefinitionService profileDefinitionService;
    @Mock
    private FieldConversionService fieldConversionService;
    @Mock
    private FieldMapper fieldMapper;


    private ProfileFacade profileFacade;

    @Before
    public void setup() throws NoSuchProfileException {
        profileFacade = new ProfileFacadeImpl(profileDefinitionService, fieldConversionService, fieldMapper);

        final ProfileDefinitionV4 profileDefinition = mock(ProfileDefinitionV4.class);
        final FieldDefinition dateField = FieldDefinition.builder().type(MappingType.date).build();
        final FieldDefinition integerField = FieldDefinition.builder().type(MappingType.integer).build();
        final FieldDefinition boolField = FieldDefinition.builder().type(MappingType.bool).build();
        final Map<String, FieldDefinition> fieldDefinitionMap = new HashMap<>();
        fieldDefinitionMap.put("testDate", dateField);
        fieldDefinitionMap.put("testInteger", integerField);
        fieldDefinitionMap.put("testBool", boolField);

        when(profileDefinition.getMetadata()).thenReturn(fieldDefinitionMap);
        when(profileDefinitionService.getProfileDefinition(anyString(), any(User.class))).thenReturn(profileDefinition);
        when(fieldMapper.convertToContentApiFieldFromRepositoryField(anyString(), anyString())).thenAnswer(i -> i.getArguments()[1]);//return second argument

    }

    @Test
    public void convertLosAngelesDateToTimeStamp() throws NoSuchProfileException, ParseException, UndefinedFieldException {
        final GregorianCalendar value = new GregorianCalendar(2018,06,11);
        value.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testDate", value);
        assertEquals(Long.class,result.getClass());
        assertEquals(1531292400000L, result);
    }
    @Test
    public void convertBrusselsDateToTimeStamp() throws NoSuchProfileException, ParseException, UndefinedFieldException {
        final GregorianCalendar value = new GregorianCalendar(2018,06,11);
        value.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testDate", value);
        assertEquals(Long.class,result.getClass());
        assertEquals(1531260000000L, result);
    }

    @Test
    public void convertInteger() throws NoSuchProfileException, UndefinedFieldException {
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testInteger", 100);
        assertEquals(Integer.class,result.getClass());
        assertEquals(100, result);
    }

    @Test
    public void convertTimeStampToDate() throws NoSuchProfileException, ParseException {
        final GregorianCalendar expectedValue = new GregorianCalendar(2018,06,11);
        expectedValue.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles")); //expected to convert to LA timezone
        final Object result = profileFacade.convertToRepoDataType("testProfile", mock(User.class), "testDate", 1531292400000L);
        assertEquals(Date.class,result.getClass());
        assertEquals(expectedValue.getTime(), result);
    }

    @Test
    public void convertBool() throws NoSuchProfileException, UndefinedFieldException {
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testBool", true);
        assertEquals(Boolean.class,result.getClass());
        assertEquals(true, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidDateThrowsIllegalArgumentException() throws NoSuchProfileException, UndefinedFieldException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testDate", BigInteger.TEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidIntegerThrowsIllegalArgumentException() throws NoSuchProfileException, UndefinedFieldException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testInteger", "invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidBoolThrowsIllegalArgumentException() throws NoSuchProfileException, UndefinedFieldException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testBool", 1);
    }

    @Test(expected = UndefinedFieldException.class)
    public void convertingFieldNotInProfileDefinitionThrowsUndefinedFieldException() throws NoSuchProfileException, UndefinedFieldException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testUnknown", "invalid");
    }

    @Test
    public void convertSearchMetadataToContentApiDataTypes() throws NoSuchProfileException {
        final String property1Name = "testInteger";
        final String property2Name = "testDate";
        final String property3Name = "testBool";
        final GregorianCalendar property2Value = new GregorianCalendar(2018,06,11);
        property2Value.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));


        final LinkedTreeMap<String, Object> properties = new LinkedTreeMap<>();
        properties.put(property1Name,"100");
        properties.put(property2Name,property2Value);
        properties.put(property3Name,true);
        final ResultNodeRepresentation resultNode = mock(ResultNodeRepresentation.class);
        when(resultNode.getProperties()).thenReturn(properties);

        final Map<String, Object>  result = profileFacade.convertMetadataToContentApiDataTypes(resultNode, mock(User.class),"testProfile");
        assertEquals(3, result.size());
        assertEquals(Integer.class,result.get(property1Name).getClass());
        assertEquals(100, result.get(property1Name));
        assertEquals(Long.class,result.get(property2Name).getClass());
        assertEquals(1531292400000L, result.get(property2Name));
        assertEquals(Boolean.class,result.get(property3Name).getClass());
        assertEquals(true, result.get(property3Name));
    }
    @Test
    public void convertCMISMetadataToContentApiDataTypes() throws NoSuchProfileException {
        final String property1Name = "testInteger";
        final Property property1 = mock(Property.class);
        when(property1.getLocalName()).thenReturn(property1Name);
        when(property1.getValue()).thenReturn("100");


        final String property2Name = "testDate";
        final GregorianCalendar property2Value = new GregorianCalendar(2018,06,11);
        property2Value.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        final Property property2 = mock(Property.class);
        when(property2.getLocalName()).thenReturn(property2Name);
        when(property2.getValue()).thenReturn(property2Value);

        final String property3Name = "testBool";
        final Property property3 = mock(Property.class);
        when(property3.getLocalName()).thenReturn(property3Name);
        when(property3.getValue()).thenReturn(true);

        final Document cmisDocument = mock(Document.class);
        when(cmisDocument.getProperties()).thenReturn(Arrays.asList(property1,property2, property3));

        final Map<String, Object>  result = profileFacade.convertMetadataToContentApiDataTypes(cmisDocument, mock(User.class),"testProfile");
        assertEquals(3, result.size());
        assertEquals(Integer.class,result.get(property1Name).getClass());
        assertEquals(100, result.get(property1Name));
        assertEquals(Long.class,result.get(property2Name).getClass());
        assertEquals(1531292400000L, result.get(property2Name));
        assertEquals(Boolean.class,result.get(property3Name).getClass());
        assertEquals(true, result.get(property3Name));
    }
    @Test
    public void convertMetadataFieldToContentApiDataType() throws NoSuchProfileException {
        final Map<String, Object>  result = profileFacade.convertMetadataFieldToContentApiDataType(mock(User.class),"testProfile","testInteger",100);
        assertEquals(1, result.size());
        assertEquals(Integer.class,result.get("testInteger").getClass());
        assertEquals(100, result.get("testInteger"));
    }

    @Test
    public void convertMetadataFieldToContentApiDataTypeShouldIgnoreAlfrescoSystemPrefix() throws NoSuchProfileException {
        final String sysField = ALFRESCO_SYSTEM_PREFIX + "test";
        final Map<String, Object>  result = profileFacade.convertMetadataFieldToContentApiDataType(mock(User.class),"testProfile",sysField,"testVal");
        assertEquals(0, result.size());
    }
}
