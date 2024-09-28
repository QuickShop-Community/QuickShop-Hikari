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

  public static double divide(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).divide(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static double multiply(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).multiply(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

  public static double subtract(final double number1, final double number2) {

    return (BigDecimal.valueOf(number1).subtract(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
  }

}
