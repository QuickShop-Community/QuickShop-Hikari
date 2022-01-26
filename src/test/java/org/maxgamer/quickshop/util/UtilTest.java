/*
 * This file is a part of project QuickShop, the name is UtilTest.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

public class UtilTest {

    @Test
    public void array2String() {
        String sample = "A, B, C";
        Assertions.assertEquals(sample, Util.array2String(new String[]{"A", "B", "C"}));
    }

    @Test
    public void boolean2Status() {
        Assertions.assertEquals(Util.boolean2Status(true), "Enabled");
        Assertions.assertEquals(Util.boolean2Status(false), "Disabled");
    }

    @Test
    public void isClassAvailable() {
        Assertions.assertTrue(Util.isClassAvailable(getClass().getName()));
        Assertions.assertFalse(Util.isClassAvailable("random.Class"));
    }

    @Test
    public void isUUID() {
        Assertions.assertTrue(Util.isUUID("b188beda-8bfb-ed66-65e5-25147a4617cf"));
        Assertions.assertFalse(Util.isUUID("b188beda8bfbed6665e525147a4617cf"));
        Assertions.assertFalse(Util.isUUID("?"));
    }

    @Test
    public void list2String() {
        String sample = "1, 2, 3, 4, 5";
        Assertions.assertEquals(sample, Util.list2String(Arrays.asList("1", "2", "3", "4", "5")));
    }

    @Test
    public void firstUppercase() {
        Assertions.assertEquals("Quickshop", Util.firstUppercase("quickshop"));
    }

    @Test
    public void mergeArgs() {
        String[] args = new String[3];
        args[0] = "yaa";
        args[1] = "hoo";
        args[2] = "woo";
        Assertions.assertEquals("yaa hoo woo", Util.mergeArgs(args));
    }

    @Test
    public void testArray2String() {
        String[] array = new String[]{"aaa", "bbb", "ccc", "ddd"};
        Assertions.assertEquals("aaa, bbb, ccc, ddd", Util.array2String(array));
    }

    @Test
    public void testIsClassAvailable() {
        Assertions.assertTrue(Util.isClassAvailable("java.lang.String"));
        Assertions.assertFalse(Util.isClassAvailable("java.lang.NotExistedClassLoL"));
    }

    @Test
    public void isMethodAvailable() {
        Assertions.assertTrue(Util.isMethodAvailable(String.class.getName(), "toLowerCase"));
        Assertions.assertFalse(Util.isMethodAvailable(String.class.getName(), "P90 RUSH B"));
    }

    @Test
    public void testIsUUID() {
        UUID uuid = UUID.randomUUID();
        Assertions.assertTrue(Util.isUUID(uuid.toString()));
        Assertions.assertTrue(Util.isUUID(Util.getNilUniqueId().toString()));
        Assertions.assertFalse(Util.isUUID(uuid.toString().replace("-", "")));
    }

    @Test
    public void prettifyText() {
        Assertions.assertEquals("Diamond", Util.prettifyText("DIAMOND"));
    }

    @Test
    public void testFirstUppercase() {
        Assertions.assertEquals("Foobar", Util.firstUppercase("foobar"));
    }

    @Test
    public void testMergeArgs() {
    }

    @Test
    public void getNilUniqueId() {
        Assertions.assertEquals(new UUID(0, 0), Util.getNilUniqueId());
    }
}
