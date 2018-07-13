package edu.uw.edm.contentapi2.service.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DataTypeUtils {


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

    public static Object convertToDate(Object value) {
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
        long timestamp = 0;
        if (value == null) {
            // NOOP
        } else if (value instanceof Calendar) {
            final Calendar cal = (Calendar) value;
            timestamp = cal.getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Unhandled Data Type: " + value.getClass());
        }
        return timestamp;
    }

}
