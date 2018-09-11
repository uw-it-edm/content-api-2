package edu.uw.edm.contentapi2.service.util;

import org.junit.Test;

import java.math.BigInteger;
import java.time.format.DateTimeParseException;
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
    public void whenConvertToBooleanInvalidStampThrowIllegalArgumentException() {
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
    public void whenConvertToDateInvalidStampThrowIllegalArgumentException() {
        DataTypeUtils.convertToLosAngelesDate("invalid");
    }

    @Test
    public void convertStringToInteger() {
        final Object result = DataTypeUtils.convertToInteger("10");
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertIntToInteger() {
        final Object result = DataTypeUtils.convertToInteger(10);
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertBigIntegerToInteger() {
        final Object result = DataTypeUtils.convertToInteger(BigInteger.TEN);
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertLongToInteger() {
        final Object result = DataTypeUtils.convertToInteger(Long.valueOf(10L));
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test
    public void convertDoubleToInteger() {
        final Object result = DataTypeUtils.convertToInteger(Double.valueOf(10.0));
        assertThat(result, instanceOf(Integer.class));
        assertEquals(10, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToIntegerInvalidStampThrowIllegalArgumentException() {
        DataTypeUtils.convertToInteger(new Date());
    }

    @Test(expected = ArithmeticException.class)
    public void whenConvertBigIntegerToIntegerLosesDataThrow() {
        final BigInteger tooBig = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        DataTypeUtils.convertToInteger(tooBig);
    }

    @Test(expected = ArithmeticException.class)
    public void whenConvertLongToIntegerLosesDataThrow() {
        final Double tooMuchPrecision = 123.45;
        DataTypeUtils.convertToInteger(tooMuchPrecision);
    }

    @Test(expected = ArithmeticException.class)
    public void whenConvertDoubleToIntegerLosesDataThrow() {
        Long tooBig = Long.valueOf(Integer.MAX_VALUE);
        tooBig += Integer.MAX_VALUE;
        DataTypeUtils.convertToInteger(tooBig);
    }

    @Test
    public void convertLosAngelesCalendarToTimeStamp() {
        final GregorianCalendar value = new GregorianCalendar(2018, 06, 11);
        value.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        final Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531292400000L, result);
    }

    @Test
    public void convertBrusselsCalendarToTimeStamp() {
        final GregorianCalendar value = new GregorianCalendar(2018, 06, 11);
        value.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        final Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531260000000L, result);
    }

    @Test
    public void convertLosAngelesDateToTimeStamp() {
        final GregorianCalendar calendar = new GregorianCalendar(2018, 06, 11);
        calendar.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        final Date value = calendar.getTime();
        final Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531292400000L, result);
    }

    @Test
    public void convertBrusselsDateToTimeStamp() {
        final GregorianCalendar calendar = new GregorianCalendar(2018, 06, 11);
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        final Date value = calendar.getTime();
        final Object result = DataTypeUtils.convertToTimeStamp(value);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1531260000000L, result);
    }

    @Test
    public void convertLosAngelesIso8601StringToTimeStamp() {
        final String iso8601dateString = "2018-09-11T15:16:53.988-0700";
        final Object result = DataTypeUtils.convertToTimeStamp(iso8601dateString);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1536704213988L, result);
    }

    @Test
    public void convertBrusselsIso8601StringToTimeStamp() {
        final String iso8601dateString = "2018-09-12T00:20:00.470+0200";
        final Object result = DataTypeUtils.convertToTimeStamp(iso8601dateString);
        assertThat(result, instanceOf(Long.class));
        assertEquals(1536704400470L, result);
    }

    @Test(expected = DateTimeParseException.class) //TODO: should we be catching this and throwing an IllegalArgumentException?
    public void whenConvertToTimeInvalidStampStringThrowIllegalArgumentException() {
        DataTypeUtils.convertToTimeStamp("invalidString");
    }
    @Test(expected = IllegalArgumentException.class)
    public void whenConvertToTimeInvalidStampThrowIllegalArgumentException() {
        DataTypeUtils.convertToTimeStamp(BigInteger.TEN);
    }

}