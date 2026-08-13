package com.ghostchu.quickshop.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class CommonUtil {

  private CommonUtil() {

  }

  public static boolean isEmptyComponent(@Nullable final Component component) {

    if(component == null) {
      return true;
    }
    if(component.equals(Component.empty())) {
      return true;
    }
    return component.equals(Component.text(""));
  }

  public static String legacyYamlKeyToNamespacedKey(@NotNull final String key) {
    final String normalized = key.toLowerCase(Locale.ROOT);
    final int index = normalized.indexOf('.');

    if (index == -1) {
      return normalized;
    }

    return normalized.substring(0, index) + ":" + normalized.substring(index + 1);
  }
}
