/*
 * Copyright 2011 StackFrame, LLC
 * All rights reserved.
 */
package com.stackframe.util.test;

import com.stackframe.util.FixedMapMaker;
import java.util.Map;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mcculley
 */
public class FixedMapMakerTest {

    public FixedMapMakerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimple() {
        FixedMapMaker maker = new FixedMapMaker(new String[]{"speed"});
        Map<String, Object> map = maker.make();
        map.put("speed", 12);
        Assert.assertEquals(12, map.get("speed"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPut() {
        FixedMapMaker maker = new FixedMapMaker(new String[]{"speed"});
        Map<String, Object> map = maker.make();
        map.put("invalid", 12);
    }

    @Test()
    public void testInvalidGet() {
        FixedMapMaker maker = new FixedMapMaker(new String[]{"speed"});
        Map<String, Object> map = maker.make();
        map.put("speed", 12);
        Assert.assertNull(map.get("invalid"));
    }

    @Test()
    public void testMultiple() {
        FixedMapMaker maker = new FixedMapMaker(new String[]{"speed", "size", "shape"});
        Map<String, Object> map = maker.make();
        map.put("speed", 12);
        map.put("size", 4);
        map.put("shape", "square");
        Assert.assertNull(map.get("invalid"));
        Assert.assertEquals(4, map.get("size"));
        Assert.assertEquals("square", map.get("shape"));
        Assert.assertEquals(12, map.get("speed"));
        Assert.assertEquals(3, map.size());
    }
}
