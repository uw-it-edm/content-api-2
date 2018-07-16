package edu.uw.edm.contentapi2.service.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DataTypeUtilsTest {
    @Test
    public void convertBoolToBoolean() {
        final Object trueResult = DataTypeUtils.convertToBoolean(Boolean.TRUE);
        assertThat(trueResult, instanceOf(Boolean.class));
        assertEquals(true, trueResult);

        final Object falseResult = DataTypeUtils.convertToBoolean(Boolean.FALSE);
        assertThat(falseResult, instanceOf(Boolean.class));
        assertEquals(false, falseResult);

    }

    @Test
    public void convertStringToBoolean() {
        final Object trueResult = DataTypeUtils.convertToBoolean("true");
        assertThat(trueResult, instanceOf(Boolean.class));
        assertEquals(true, trueResult);

        final Object falseResult = DataTypeUtils.convertToBoolean("false");
        assertThat(falseResult, instanceOf(Boolean.class));
        assertEquals(false, falseResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToBooleanInvalidStampThrowIllegalArumentException() {
        DataTypeUtils.convertToBoolean(1);
    }

    @Test
    public void convertTimeStampToLosAngelesDate() {
        final GregorianCalendar expectedDate = new GregorianCalendar(2018, 06, 11);
        expectedDate.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        final Object result = DataTypeUtils.convertToLosAngelesDate(1531292400000L);
        assertThat(result, instanceOf(Date.class));
        assertEquals(expectedDate.getTime(), result);

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToDateInvalidStampThrowIllegalArumentException() {
        DataTypeUtils.convertToLosAngelesDate("invalid");
    }

    @Test
    public void convertStringToInteger() {
        Object result = DataTypeUtils.convertToInteger("10");
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertIntToInteger() {
        Object result = DataTypeUtils.convertToInteger(10);
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertBigIntegerToInteger() {
        Object result = DataTypeUtils.convertToInteger(BigInteger.TEN);
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToIntegerInvalidStampThrowIllegalArumentException() {
        DataTypeUtils.convertToInteger(new Date());
    }

    @Test(expected = ArithmeticException.class)
    public void whenConvertToIntegerLosesDataThrow() {
        BigInteger tooBig = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        DataTypeUtils.convertToInteger(tooBig);
    }


    @Test
    public void convertLosAngelesDateToTimeStamp(){
        final GregorianCalendar value = new GregorianCalendar(2018,06,11);
        value.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531292400000L, result);
    }

    @Test
    public void convertBrusselsDateToTimeStamp(){
        final GregorianCalendar value = new GregorianCalendar(2018,06,11);
        value.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531260000000L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToTimeInvalidStampThrowIllegalArumentException() {
        DataTypeUtils.convertToTimeStamp("invalid");
    }


}