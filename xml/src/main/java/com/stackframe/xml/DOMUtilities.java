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
package com.stackframe.xml;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities for use with XML DOM objects.
 *
 * @author mcculley
 */
public class DOMUtilities {

    private DOMUtilities() {
        // Inhibit construction as this is a utility class.
    }

    /**
     * A convenience view that gets the node values out of a NodeList.
     *
     * @param nodeList this NodeList to get values out of
     * @return an Iterable of the values
     */
    public static Iterable<String> values(NodeList nodeList) {
        return Iterables.transform(iterable(nodeList), new Function<Node, String>() {
            @Override
            public String apply(Node f) {
                return f.getNodeValue();
            }
        });
    }

    /**
     * Get a view of a NodeList as an Iterator.
     *
     * @param nodeList the NodeList to iterate over
     * @return an Iterator that iterates over nodeList
     */
    public static Iterator<Node> iterator(final NodeList nodeList) {
        return new AbstractIterator<Node>() {
            private int index;

            @Override
            protected Node computeNext() {
                if (index == nodeList.getLength()) {
                    return endOfData();
                }

                return nodeList.item(index++);
            }
        };
    }

    /**
     * Get a view of a NodeList as an Iterable.
     *
     * @param nodeList the NodeList to iterate over
     * @return an Iterable that iterates over nodeList
     */
    public static Iterable<Node> iterable(final NodeList nodeList) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return DOMUtilities.iterator(nodeList);
            }
        };
    }
}