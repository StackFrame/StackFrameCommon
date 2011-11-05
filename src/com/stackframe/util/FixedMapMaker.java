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
package com.stackframe.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A factory that makes maps with a fixed set of keys. A collection of such maps uses less memory than a collection of HashMap
 * objects which explicitly define the keys.
 *
 * @author Gene McCulley
 */
public class FixedMapMaker {

    private final String[] keys;

    /**
     * Create a FixedMapMaker for a defined set of keys.
     *
     * @param keys the keys to use
     */
    public FixedMapMaker(String[] keys) {
        this.keys = keys.clone();
    }

    private int indexOf(String name) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Make a new Map defined by this FixedMapMaker.
     *
     * @return a new Map that uses the keys defined in this FixedMapMaker
     */
    public <V> Map<String, V> make() {
        return new AbstractMap<String, V>() {

            private final Object[] values = new Object[keys.length];

            @Override
            public Object put(String k, Object v) {
                int index = indexOf(k);
                if (index == -1) {
                    throw new IllegalArgumentException(k + " is not a valid key");
                }

                Object old = values[index];
                values[index] = v;
                return old;
            }

            private Map.Entry<String, V> makeEntry(final int index) {
                return new Map.Entry<String, V>() {

                    @Override
                    public String getKey() {
                        return keys[index];
                    }

                    @Override
                    public V getValue() {
                        return (V) values[index];
                    }

                    @Override
                    public V setValue(V v) {
                        V old = (V) values[index];
                        values[index] = v;
                        return old;
                    }
                };
            }

            @Override
            public Set<Entry<String, V>> entrySet() {
                return new AbstractSet<Entry<String, V>>() {

                    @Override
                    public Iterator<Entry<String, V>> iterator() {
                        return new Iterator<Entry<String, V>>() {

                            /**
                             * Index of element to be returned by subsequent call to next().
                             */
                            int cursor = 0;

                            /**
                             * Index of element returned by most recent call to next().  Reset to -1 if this element is deleted by
                             * a call to remove().
                             */
                            int lastRet = -1;

                            @Override
                            public boolean hasNext() {
                                return cursor != size();
                            }

                            @Override
                            public Entry<String, V> next() {
                                try {
                                    Entry<String, V> next = makeEntry(cursor);
                                    lastRet = cursor++;
                                    return next;
                                } catch (IndexOutOfBoundsException e) {
                                    throw new NoSuchElementException();
                                }
                            }

                            @Override
                            public void remove() {
                                if (lastRet == -1) {
                                    throw new IllegalStateException();
                                }

                                values[lastRet] = null;
                                lastRet = -1;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return keys.length;
                    }
                };
            }
        };
    }
}
