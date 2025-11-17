package com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl;

import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.key.CrateKey;

public class ExcellentCratesCheck implements ItemCheck {

  /**
   * Check if this check applies to the specified ItemStack
   *
   * @param stack the ItemStack to check
   *
   * @return true if the check applies to the ItemStack, otherwise false
   */
  @Override
  public boolean applies(final @Nullable ItemStack stack) {

    return stack != null && CratesAPI.getKeyManager().isKey(stack);
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

    final CrateKey originalKey = (stack != null)? CratesAPI.getKeyManager().getKeyByItem(stack) : null;
    final CrateKey compareKey = (compare != null)? CratesAPI.getKeyManager().getKeyByItem(compare) : null;

    if(originalKey == null) {

      return compareKey == null;
    }

    if(compareKey == null) {

      return false;
    }

    return originalKey.getId().equals(compareKey.getId());
  }
}
