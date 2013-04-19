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
package com.stackframe.base;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 * Utilities for working with the Number abstraction.
 *
 * @author mcculley
 */
public class Numbers {

    /**
     * Get a Set of all positive integers. This is useful for making Map objects that can work on any integer input. You do not want
     * to try to iterate the returned Set.
     *
     * The real utility in this is to deal with Java's lame type system. Because a boxed int and a boxed long of the same value
     * don't have equality as far as a hash map is concerned, it is hard to use a Map when one doesn't know if the key will be an
     * int or a long (as happens when using the JSP expression language).
     *
     * @return a Set of all long and int values.
     */
    public static Set<Number> positiveIntegers() {
        Set<? extends Number> longKeys = ContiguousSet.create(Range.greaterThan(0L), DiscreteDomain.longs());
        Set<? extends Number> intKeys = ContiguousSet.create(Range.greaterThan(0), DiscreteDomain.integers());
        return Sets.union(longKeys, intKeys);
    }
}
