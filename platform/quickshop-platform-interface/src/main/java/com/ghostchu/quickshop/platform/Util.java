package com.ghostchu.quickshop.platform;

public class Util {

    private Util() {
    }

    public static boolean methodExists(Class<?> clazz, String methodName) {
        try {
            clazz.getDeclaredMethod(methodName);
            return true;
        } catch (NoSuchMethodException var3) {
            return false;
        }
    }
}
