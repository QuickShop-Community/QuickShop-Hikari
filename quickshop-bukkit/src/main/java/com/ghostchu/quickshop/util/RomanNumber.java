/*
 *  This file is a part of project QuickShop, the name is RomanNumber.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.util;

import java.util.TreeMap;

/**
 * @author "Ben-Hur Langoni Junior" on StackOverFlow page "<a href="https://stackoverflow.com/a/19759564">https://stackoverflow.com/a/19759564</a>"
 */
public class RomanNumber {
    private static final TreeMap<Integer, String> MAP = new TreeMap<>();

    static {
        MAP.put(1000, "M");
        MAP.put(900, "CM");
        MAP.put(500, "D");
        MAP.put(400, "CD");
        MAP.put(100, "C");
        MAP.put(90, "XC");
        MAP.put(50, "L");
        MAP.put(40, "XL");
        MAP.put(10, "X");
        MAP.put(9, "IX");
        MAP.put(5, "V");
        MAP.put(4, "IV");
        MAP.put(1, "I");
    }

    public static String toRoman(Integer number) {
        return toRoman(number == null ? 1 : number);
    }

    public static String toRoman(int number) {
        Integer l = MAP.floorKey(number);
        if (l == null) {
            return MAP.get(1);
        }
        if (number == l) {
            return MAP.get(number);
        }
        return MAP.get(l) + toRoman(number - l);
    }

}
