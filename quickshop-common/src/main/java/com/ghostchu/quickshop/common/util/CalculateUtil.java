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

    public static double add(double number1, double number2) {
        return (BigDecimal.valueOf(number1).add(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
    }

    public static double divide(double number1, double number2) {
        return (BigDecimal.valueOf(number1).divide(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
    }

    public static double multiply(double number1, double number2) {
        return (BigDecimal.valueOf(number1).multiply(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
    }

    public static double subtract(double number1, double number2) {
        return (BigDecimal.valueOf(number1).subtract(BigDecimal.valueOf(number2), MATH_CONTEXT)).doubleValue();
    }


}
