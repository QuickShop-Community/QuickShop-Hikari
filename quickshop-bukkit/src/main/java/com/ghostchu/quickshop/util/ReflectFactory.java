package com.ghostchu.quickshop.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * ReflectFactory is library builtin QuickShop to get/execute stuff that cannot be access with
 * BukkitAPI with reflect way.
 *
 * @author Ghost_chu
 */
public class ReflectFactory {

  private static String nmsVersion;

  private ReflectFactory() {

  }

  @NotNull
  public static String getNMSVersion() {

    if(nmsVersion == null) {
      final String name = Bukkit.getServer().getClass().getPackage().getName();
      nmsVersion = name.substring(name.lastIndexOf('.') + 1);
    }
    return nmsVersion;
  }
}
