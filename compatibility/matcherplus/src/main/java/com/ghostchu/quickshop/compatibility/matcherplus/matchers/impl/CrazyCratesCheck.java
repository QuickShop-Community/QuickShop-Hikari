package com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl;

import com.badbones69.crazycrates.paper.api.enums.other.keys.ItemKeys;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class CrazyCratesCheck implements ItemCheck {

  private final String defaultValue = "no-key";

  /**
   * Check if this check applies to the specified ItemStack
   *
   * @param stack the ItemStack to check
   *
   * @return true if the check applies to the ItemStack, otherwise false
   */
  @Override
  public boolean applies(final @Nullable ItemStack stack) {

    return stack != null && stack.getPersistentDataContainer().has(ItemKeys.crate_key.getNamespacedKey(), PersistentDataType.STRING);
  }

  /**
   * Checks if two ItemStack objects match each other.
   *
   * @param stack   the first ItemStack to compare
   * @param compare the second ItemStack to compare
   *
   * @return true if the two ItemStack objects match, false otherwise
   */
  @Override
  public boolean matches(final @Nullable ItemStack stack, final @Nullable ItemStack compare) {

    final String originalKey = (stack != null)? stack.getPersistentDataContainer().getOrDefault(ItemKeys.crate_key.getNamespacedKey(), PersistentDataType.STRING, defaultValue) : defaultValue;
    final String compareKey = (compare != null)? compare.getPersistentDataContainer().getOrDefault(ItemKeys.crate_key.getNamespacedKey(), PersistentDataType.STRING, defaultValue) : defaultValue;

    return originalKey.equals(compareKey);
  }
}
