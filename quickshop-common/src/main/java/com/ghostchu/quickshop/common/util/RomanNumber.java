package com.ghostchu.quickshop.common.util;

import java.util.TreeMap;

/**
 * @author "Ben-Hur Langoni Junior" on StackOverFlow page "<a
 * href="https://stackoverflow.com/a/19759564">https://stackoverflow.com/a/19759564</a>"
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

  private RomanNumber() {

  }

  public static String toRoman(final Integer number) {

    return toRoman(number == null? 1 : number);
  }

  public static String toRoman(final int number) {

    final Integer l = MAP.floorKey(number);
    if(l == null) {
      return MAP.get(1);
    }
    if(number == l) {
      return MAP.get(number);
    }
    return MAP.get(l) + toRoman(number - l);
  }

}
