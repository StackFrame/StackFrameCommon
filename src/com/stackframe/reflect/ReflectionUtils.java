/*
 * Copyright 2011-2013 StackFrame, LLC
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stackframe.reflect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * Useful utilities that leverage reflection.
 *
 * @author Gene McCulley
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
     * Print out all of the fields in an object. This is useful as a debugging implementation of toString() that classes can
     * delegate to.
     *
     * @param o the Object to print
     * @return a String that contains all of the field names and their values
     */
    public static String toString(Object o) {
        return toString(o.getClass(), o).toString();
    }

    /**
     * Find a property on a class.
     *
     * @param c the Class to look for the property on
     * @param property the property to look for
     * @return a PropertyDescriptor for the property, or <code>null</code> if the property is not found
     * @throws IntrospectionException if there is trouble introspecting Class c
     */
    private static PropertyDescriptor propertyDescriptor(Class c, String property) throws IntrospectionException {
        if (!c.isInterface()) {
            // Look in interfaces first for the property as they are guaranteed to be publicly invokable.
            Class[] interfaces = c.getInterfaces();
            for (Class iface : interfaces) {
                PropertyDescriptor pd = propertyDescriptor(iface, property);
                if (pd != null) {
                    return pd;
                }
            }
        }

        PropertyDescriptor[] pds = Introspector.getBeanInfo(c).getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(property)) {
                return pd;
            }
        }

        return null;
    }

    /**
     * Given a simple boolean property on a class, make a Predicate which invokes it.
     *
     * @param <T> The JavaBeans type to look up the property on
     * @param c the Class of the JavaBeans type, constrained to T
     * @param property the property name
     * @return a Predicate which invokes the getter for the specified property
     */
    public static <T> Predicate<T> predicateForProperty(Class<T> c, String property) {
        try {
            PropertyDescriptor pd = propertyDescriptor(c, property);
            if (pd == null) {
                throw new IllegalArgumentException(String.format("property '%s' does not exist on class '%s'", property, c.getName()));
            }

            assert pd.getPropertyType() == boolean.class;
            final Method getter = pd.getReadMethod();
            assert getter.getParameterTypes().length == 0;
            return new Predicate<T>() {
                @Override
                public boolean apply(T t) {
                    try {
                        return (Boolean) getter.invoke(t, (Object[]) null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (IntrospectionException ie) {
            throw new AssertionError(ie);
        }
    }

    /**
     * Given a simple boolean property on a class, make a Predicate which invokes it.
     *
     * Note that the version of this function which takes a Class parameter is better performing as it will only need to look up the
     * method at creation time instead of at each invocation.
     *
     * @param property the property name
     * @return a Predicate which invokes the getter for the specified property
     */
    public static <T> Predicate<T> predicateForProperty(final String property) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T t) {
                try {
                    Class c = t.getClass();
                    PropertyDescriptor pd = propertyDescriptor(c, property);
                    if (pd == null) {
                        throw new IllegalArgumentException(String.format("property '%s' does not exist on class '%s'", property, c.getName()));
                    }

                    assert pd.getPropertyType() == boolean.class;
                    Method getter = pd.getReadMethod();
                    assert getter.getParameterTypes().length == 0;
                    return (Boolean) getter.invoke(t, (Object[]) null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Given a simple property on a class, make a Function which invokes the getter.
     *
     * @param <T> The JavaBeans type to look up the property on
     * @param c the Class of the JavaBeans type, constrained to T
     * @param property the property name
     * @return a Function which invokes the getter for the specified property
     */
    public static <F, T> Function<F, T> functionForProperty(Class<F> f, String property) {
        try {
            PropertyDescriptor pd = propertyDescriptor(f, property);
            if (pd == null) {
                throw new IllegalArgumentException(String.format("property '%s' does not exist on class '%s'", property, f.getName()));
            }

            final Method getter = pd.getReadMethod();
            assert getter.getParameterTypes().length == 0;
            return new Function<F, T>() {
                @Override
                public T apply(F f) {
                    try {
                        return (T) getter.invoke(f, (Object[]) null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (IntrospectionException ie) {
            throw new AssertionError(ie);
        }
    }

    /**
     * Given a simple property, make a Function which invokes the getter.
     *
     * Note that the version of this function which takes a Class parameter is better performing as it will only need to look up the
     * method at creation time instead of at each invocation.
     *
     * @param property the property name
     * @return a Function which invokes the getter for the specified property
     */
    public static <F, T> Function<F, T> functionForProperty(final String property) {
        return new Function<F, T>() {
            @Override
            public T apply(F f) {
                Class<F> c = (Class<F>) f.getClass();
                try {
                    PropertyDescriptor pd = propertyDescriptor(c, property);
                    if (pd == null) {
                        throw new IllegalArgumentException(String.format("property '%s' does not exist on class '%s'", property, c.getName()));
                    }

                    Method getter = pd.getReadMethod();
                    assert getter.getParameterTypes().length == 0;
                    return (T) getter.invoke(f, (Object[]) null);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("trouble with property '%s' on class '%s'", property, c.getName()), e);
                }
            }
        };
    }
}