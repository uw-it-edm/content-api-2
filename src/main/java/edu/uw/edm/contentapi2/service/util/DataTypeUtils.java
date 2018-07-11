package edu.uw.edm.contentapi2.service.util;

import java.util.Calendar;

public class DataTypeUtils {
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

    public static Object convertToInteger(Object value) {
        Integer integer = null;
        if (value == null) {
            // NOOP
        } else if (value instanceof Integer) {
            integer = (Integer) value;
        } else if (value instanceof String) {
            integer = Integer.valueOf((String) value);
        } else {
            throw new IllegalArgumentException("Unhandled Data Type" + value.getClass());
        }
        return integer;
    }
}
