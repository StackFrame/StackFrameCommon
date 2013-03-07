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
package com.stackframe.sql;

import com.stackframe.util.FixedMapMaker;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility functions for use with the java.sql package.
 *
 * @author Gene McCulley
 */
public class SQLUtilities {

    private SQLUtilities() {
        // Inhibit construction as this class has only static functions.
    }

    private static String[] getColumnNames(ResultSetMetaData rsmd) throws SQLException {
        int columnCount = rsmd.getColumnCount();
        String[] names = new String[columnCount];
        for (int i = 1; i < columnCount + 1; i++) {
            String name = rsmd.getColumnName(i);
            names[i - 1] = name;
        }

        return names;
    }

    /**
     * Given a PreparedStatement, execute it and load all of the values into a List of Map objects, keyed by column name.
     *
     * @param statement the PreparedStatement to execute
     * @return a List of Map objects where keys are column names and values are the values retrieved from the SQL query
     * @throws SQLException if a SQLException was thrown when executing the query
     */
    public static List<Map<String, Object>> load(PreparedStatement statement) throws SQLException {
        ResultSet rs = statement.executeQuery();
        try {
            List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
            ResultSetMetaData rsmd = rs.getMetaData();
            FixedMapMaker mapMaker = new FixedMapMaker(getColumnNames(rsmd));
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = mapMaker.make();
                for (int i = 1; i < columnCount + 1; i++) {
                    String name = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);
                    map.put(name, value);
                }

                entries.add(map);
            }

            return entries;
        } finally {
            rs.close();
        }
    }

    /**
     * Given a PreparedStatement, execute it and load all of the values into POJOs of a specified type.
     *
     * @param statement the PreparedStatement to execute
     * @return a List of objects of type where values retrieved from the SQL query are assigned to fields of the same name as the columns
     * @throws SQLException if a SQLException was thrown when executing the query
     * @throws AssertionError if the POJO does not have an accessible constructor or is missing a field named after a column
     */
    public static <C> List<C> load(PreparedStatement statement, Class<C> type) throws SQLException {
        ResultSet rs = statement.executeQuery();
        try {
            List<C> entries = new ArrayList<C>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                try {
                    C instance = type.newInstance();
                    for (int i = 1; i < columnCount + 1; i++) {
                        String name = rsmd.getColumnName(i);
                        Object value = rs.getObject(i);
                        Field field = type.getField(name);
                        field.set(instance, value);
                    }

                    entries.add(instance);
                } catch (InstantiationException ie) {
                    throw new AssertionError(ie);
                } catch (IllegalAccessException iae) {
                    throw new AssertionError(iae);
                } catch (NoSuchFieldException nsfe) {
                    throw new AssertionError(nsfe);
                }
            }

            return entries;
        } finally {
            rs.close();
        }
    }

    /**
     * Given a java.util.Date, return a java.sql.Date.
     *
     * @param date the java.util.Date to convert
     * @return the date in java.sql.Date
     */
    public static java.sql.Date convert(Date date) {
        if (date instanceof java.sql.Date) {
            return (java.sql.Date) date;
        } else {
            return new java.sql.Date(date.getTime());
        }
    }
}
