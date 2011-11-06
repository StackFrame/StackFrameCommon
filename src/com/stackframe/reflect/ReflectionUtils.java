/*
 * Copyright 2011 StackFrame, LLC
 * All rights reserved.
 */
package com.stackframe.reflect;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * Useful utilities that leverage reflection.
 *
 * @author mcculley
 */
public class ReflectionUtils {

    private ReflectionUtils() {
        // Inhibit construction as this is a utility class.
    }

    private static Map<String, Object> toString(Class c, Object o) {
        Map<String, Object> map = new TreeMap<String, Object>();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            if (field.isSynthetic()) {
                continue;
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            try {
                map.put(field.getName(), field.get(o));
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }

        Class superclass = c.getSuperclass();
        if (superclass != null) {
            map.putAll(toString(superclass, o));
        }

        Class[] interfaces = c.getInterfaces();
        for (Class iface : interfaces) {
            map.putAll(toString(iface, o));
        }

        return map;
    }

    /**
     * Print out all of the fields in an object.  This is useful as a debugging implementation of toString() that classes can
     * delegate to.
     *
     * @param o the Object to print
     * @return a String that contains all of the field names and their values
     */
    public static String toString(Object o) {
        return toString(o.getClass(), o).toString();
    }
}