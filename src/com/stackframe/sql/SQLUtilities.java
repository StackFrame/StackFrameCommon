/*
 * Copyright 2011 StackFrame, LLC
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
}
