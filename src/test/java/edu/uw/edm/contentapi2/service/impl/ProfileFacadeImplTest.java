package edu.uw.edm.contentapi2.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.repository.exceptions.NoSuchProfileException;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileFacade;
import edu.uw.edm.contentapi2.service.model.FieldDefinition;
import edu.uw.edm.contentapi2.service.model.MappingType;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;
import edu.uw.edm.contentapi2.service.profile.FieldConversionService;
import edu.uw.edm.contentapi2.service.profile.ProfileDefinitionService;

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
    public void convertDate() throws NoSuchProfileException, ParseException {
        final GregorianCalendar value = new GregorianCalendar(2018,06,11);
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testDate", value);
        assertEquals(Long.class,result.getClass());
        assertEquals(1531292400000L, result);
    }

    @Test
    public void convertInteger() throws NoSuchProfileException {
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testInteger", 100);
        assertEquals(Integer.class,result.getClass());
        assertEquals(100, result);
    }

    @Test
    public void convertBool() throws NoSuchProfileException {
        final Object result = profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testBool", true);
        assertEquals(Boolean.class,result.getClass());
        assertEquals(true, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidDateThrowsIllegalArgumentException() throws NoSuchProfileException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testDate", "invalid");
    }
    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidIntegerThrowsIllegalArgumentException() throws NoSuchProfileException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testInteger", "invalid");
    }
    @Test(expected = IllegalArgumentException.class)
    public void convertingInvalidBoolThrowsIllegalArgumentException() throws NoSuchProfileException {
        profileFacade.convertToContentApiDataType("testProfile", mock(User.class), "testBool", 1);
    }
}