package com.ghostchu.quickshop.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class CommonUtil {
    private CommonUtil() {
    }

    public static boolean isEmptyComponent(@Nullable Component component) {
        if (component == null) {
            return true;
        }
        if (component.equals(Component.empty())) {
            return true;
        }
        return component.equals(Component.text(""));
    }

}
