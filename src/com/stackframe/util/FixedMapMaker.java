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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A factory that makes maps with a fixed set of keys. A collection of such maps uses less memory than a collection of HashMap
 * objects which explicitly define the keys.
 *
 * @author Gene McCulley
 */
public class FixedMapMaker<K> {

    private final Map<K, Integer> keys = new HashMap<K, Integer>();

    /**
     * Create a FixedMapMaker for a defined set of keys.
     *
     * @param keys the keys to use
     */
    public FixedMapMaker(K[] keys) {
        int numKeys = keys.length;
        for (int i = 0; i < numKeys; i++) {
            this.keys.put(keys[i], i);
        }
    }

    /**
     * Make a new Map defined by this FixedMapMaker.
     *
     * @return a new Map that uses the keys defined in this FixedMapMaker
     */
    public <V> Map<K, V> make() {
        return new AbstractMap<K, V>() {

            private final Object[] values = new Object[keys.size()];

            @Override
            public V put(K k, V v) {
                Integer index = keys.get(k);
                if (index == null) {
                    throw new IllegalArgumentException(k + " is not a valid key");
                }

                V old = (V) values[index];
                values[index] = v;
                return old;
            }

            private Map.Entry<K, V> makeEntry(final K key, final int index) {
                return new Map.Entry<K, V>() {

                    @Override
                    public K getKey() {
                        return key;
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
            public Set<Entry<K, V>> entrySet() {
                return new AbstractSet<Entry<K, V>>() {

                    @Override
                    public Iterator<Entry<K, V>> iterator() {
                        return new Iterator<Entry<K, V>>() {

                            private final Iterator<Entry<K, Integer>> i = keys.entrySet().iterator();

                            /**
                             * Key of element returned by most recent call to next().  Reset to null if this element is deleted by
                             * a call to remove().
                             */
                            private Entry<K, Integer> lastRet = null;

                            @Override
                            public boolean hasNext() {
                                return i.hasNext();
                            }

                            @Override
                            public Entry<K, V> next() {
                                lastRet = i.next();
                                return makeEntry(lastRet.getKey(), lastRet.getValue());
                            }

                            @Override
                            public void remove() {
                                if (lastRet == null) {
                                    throw new IllegalStateException();
                                }

                                values[lastRet.getValue()] = null;
                                lastRet = null;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return keys.size();
                    }
                };
            }
        };
    }
}
