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
import java.util.stream.StreamSupport;

import static com.stackframe.sql.SQLUtilities.convert;

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
     * Determine if a value is contained in any supplied Ranges.
     *
     * @param <C> the class that Range is constrained to
     * @param ranges the ranges to check
     * @param value the value to check for
     * @return true if value is contained in any of the ranges
     */
    public static <C extends Comparable> boolean contains(Iterable<Range<C>> ranges, C value) {
        return StreamSupport.stream(ranges.spliterator(), false).anyMatch(r -> r.contains(value));
    }

    /**
     * Build an expression suitable for passing to JDBC as part of an SQL query from a date range.
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
            java.sql.Date lowerDate = convert(lowerEndpoint);
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
            java.sql.Date upperDate = convert(upperEndpoint);
            buf.append(String.format("%s %s '%s'", column, operator, upperDate.toString()));
        }

        return buf.toString();
    }
}
