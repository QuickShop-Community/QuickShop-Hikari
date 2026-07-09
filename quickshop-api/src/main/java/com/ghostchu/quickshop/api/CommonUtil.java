package com.ghostchu.quickshop.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    final int index = key.indexOf('.');

    if (index == -1) {
      return key;
    }

    return key.substring(0, index) + ":" + key.substring(index + 1);
  }
}
