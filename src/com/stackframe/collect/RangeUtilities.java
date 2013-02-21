/*
 * Copyright 2013 StackFrame, LLC
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stackframe.collect;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import java.util.Date;

/**
 * Useful functions for dealing with Range objects.
 *
 * @author Gene McCulley
 */
public class RangeUtilities {

    private RangeUtilities() {
        // inhibit construction of utility class
    }

    /**
     * Given a java.util.Date, return a java.sql.Date.
     *
     * FIXME: This should probably be somewhere else as it has nothing to do
     * with Range.
     *
     * @param date the java.util.Date to convert
     * @return the date in java.sql.Date
     */
    public static java.sql.Date toSQL(Date date) {
        if (date instanceof java.sql.Date) {
            return (java.sql.Date) date;
        } else {
            return new java.sql.Date(date.getTime());
        }
    }

    /**
     * Build an expression suitable for passing to JDBC as part of an SQL query
     * from a date range.
     *
     * @param column the name of the column
     * @param dateRange the Range
     * @return a String containing the expression
     */
    public static String toSQL(String column, Range<Date> dateRange) {
        StringBuilder buf = new StringBuilder();
        if (dateRange.hasLowerBound()) {
            BoundType lowerBound = dateRange.lowerBoundType();
            String operator;
            switch (lowerBound) {
                case CLOSED:
                    operator = ">=";
                    break;
                case OPEN:
                    operator = ">";
                    break;
                default:
                    throw new AssertionError("unexpected bound type " + lowerBound);
            }

            Date lowerEndpoint = dateRange.lowerEndpoint();
            java.sql.Date lowerDate = toSQL(lowerEndpoint);
            buf.append(String.format("%s %s '%s'", column, operator, lowerDate.toString()));
            if (dateRange.hasUpperBound()) {
                buf.append(" AND ");
            }
        }

        if (dateRange.hasUpperBound()) {
            BoundType upperBound = dateRange.upperBoundType();
            String operator;
            switch (upperBound) {
                case CLOSED:
                    operator = "<=";
                    break;
                case OPEN:
                    operator = "<";
                    break;
                default:
                    throw new AssertionError("unexpected bound type " + upperBound);
            }

            Date upperEndpoint = dateRange.upperEndpoint();
            java.sql.Date upperDate = toSQL(upperEndpoint);
            buf.append(String.format("%s %s '%s'", column, operator, upperDate.toString()));
        }

        return buf.toString();
    }
}
