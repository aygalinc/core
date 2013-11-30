package org.ow2.chameleon.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Check the behavior of the main class, especially the argument parsing.
 */
public class MainTest {
    @Test
    public void testParseUserProperties() throws Exception {
        String[] args = new String[] {
                "-Dflag", "-Dpair1=value1", "-Dpair2=value2"
        };

        Map<String, Object> map = Main.parseUserProperties(args);
        Assert.assertEquals(map.get("flag"), Boolean.TRUE);
        Assert.assertEquals(map.get("pair1"), "value1");
        Assert.assertEquals(map.get("pair2"), "value2");
    }

    @Test
    public void testParseUserPropertiesWithInteractive() throws Exception {
        String[] args = new String[] {
                "-Dflag", "-Dpair1=value1", "-Dpair2=value2", "--interactive"
        };

        Map<String, Object> map = Main.parseUserProperties(args);
        Assert.assertEquals(map.get("flag"), Boolean.TRUE);
        Assert.assertEquals(map.get("pair1"), "value1");
        Assert.assertEquals(map.get("pair2"), "value2");
    }
}