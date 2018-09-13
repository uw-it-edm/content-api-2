package edu.uw.edm.contentapi2.service.util;

import java.math.BigInteger;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DataTypeUtils {
    private static final String ISO_8601_DATETIME_WITH_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final DateTimeFormatter ISO_8601_DATETIME_WITH_OFFSET =  DateTimeFormatter.ofPattern(ISO_8601_DATETIME_WITH_OFFSET_PATTERN);

    public static Object convertToBoolean(Object value) {
        Boolean bool = null;
        if (value == null) {
            // NOOP
        } else if (value instanceof Boolean) {
            bool = (Boolean) value;
        } else if (value instanceof String) {
            bool = Boolean.valueOf((String) value);
        } else {
            throw new IllegalArgumentException("Unhandled Data Type" + value.getClass());
        }
        return bool;
    }

    public static Object convertToLosAngelesDate(Object value) {
        Date date = null;
        if (value == null) {
            // NOOP
        } else if (value instanceof Long) {
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            gc.setTimeInMillis((Long) value);
            date = gc.getTime();
        } else {
            throw new IllegalArgumentException("Unhandled Data Type: " + value.getClass());
        }
        return date;
    }

    public static Object convertToInteger(Object value) {
        Integer integer = null;
        if (value == null) {
            // NOOP
        } else if (value instanceof Integer) {
            integer = (Integer) value;
        } else if (value instanceof Long) {
            integer = Math.toIntExact((Long) value);
        } else if (value instanceof Double) {
            final Double doubleValue = (Double) value;
            if (doubleValue % 1 == 0) {
                integer = doubleValue.intValue();
            } else {
                throw new ArithmeticException("integer overflow: converting double value to integer");
            }
        } else if (value instanceof BigInteger) {
            integer = ((BigInteger) value).intValueExact();
        } else if (value instanceof String) {
            integer = Integer.valueOf((String) value);
        } else {
            throw new IllegalArgumentException("Unhandled Data Type" + value.getClass());
        }
        return integer;
    }

    public static Object convertToTimeStamp(Object value) {
        Long timestamp = null;
        if (value == null) {
            // NOOP
        } else if (value instanceof Calendar) {
            final Calendar cal = (Calendar) value;
            timestamp = cal.getTimeInMillis();
        } else if (value instanceof Date) {
            timestamp = ((Date) value).getTime();
        } else if (value instanceof String) {
            final TemporalAccessor parsedDate = ISO_8601_DATETIME_WITH_OFFSET.parse((String) value);
            final Instant instant = Instant.from(parsedDate);
            timestamp = instant.toEpochMilli();
        } else {
            throw new IllegalArgumentException("Unhandled Data Type: " + value.getClass());
        }
        return timestamp;
    }

}
