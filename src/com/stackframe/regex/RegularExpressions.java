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
package com.stackframe.regex;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

/**
 * Utilities for dealing with regular expressions.
 *
 * @author mcculley
 */
public class RegularExpressions {

    /**
     * Compile an Iterable of regular expressions.
     *
     * @param expressions an Iterable of Strings representing regular expressions
     * @return an Iterable of compiled Pattern objects
     */
    public static Iterable<Pattern> compile(Iterable<String> expressions) {
        ImmutableList.Builder<Pattern> b = ImmutableList.builder();
        for (String expression : expressions) {
            b.add(Pattern.compile(expression));
        }

        return b.build();
    }

    /**
     * Determine if a given input matches any of a supplied set of Patterns.
     *
     * @param patterns an Iterable of Patterns
     * @input the input to test
     * @return true if the input matches any of the patterns
     */
    public static boolean matches(Iterable<Pattern> patterns, CharSequence input) {
        for (Pattern pattern : patterns) {
            System.err.println("testing pattern='" + pattern + "' against input='" + input + "'");
            if (pattern.matcher(input).matches()) {
                System.err.println("matches!");
                return true;
            }
        }

        System.err.println("does not match");
        return false;
    }

    /**
     * Build a Predicate which will evaluate to true if any of a supplied set of Patterns matches the input.
     *
     * @param patterns the Patterns to test against
     * @return a Predicate which will evaluate to true if any of a supplied set of Patterns matches the input
     */
    public static Predicate<CharSequence> matchesPredicate(final Iterable<Pattern> patterns) {
        return new Predicate<CharSequence>() {
            @Override
            public boolean apply(CharSequence input) {
                return matches(patterns, input);
            }

            @Override
            public String toString() {
                return patterns.toString();
            }
        };
    }
}
