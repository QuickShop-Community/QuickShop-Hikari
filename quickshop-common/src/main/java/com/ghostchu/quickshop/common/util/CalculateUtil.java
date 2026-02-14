package com.ghostchu.quickshop.common.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * CalculateUtil used for calculate between doubles
 *
 * @author sandtechnology
 */
public final class CalculateUtil {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

  private CalculateUtil() {

  }

  public static double add(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).add(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static BigDecimal add(final BigDecimal number1, final BigDecimal number2) {

    return number1.add(number2, MATH_CONTEXT);
  }

  public static double divide(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).divide(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static BigDecimal divide(final BigDecimal number1, final BigDecimal number2) {

    return number1.divide(number2, MATH_CONTEXT);
  }

  public static double multiply(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).multiply(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static BigDecimal multiply(final BigDecimal number1, final BigDecimal number2) {

    return number1.multiply(number2, MATH_CONTEXT);
  }

  public static double subtract(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).subtract(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static BigDecimal subtract(final BigDecimal number1, final BigDecimal number2) {

    return number1.subtract(number2, MATH_CONTEXT);
  }

}