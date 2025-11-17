package com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl;

import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import de.dustplanet.util.SilkUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SilkSpawnerCheck implements ItemCheck {

  private final String defaultValue = "no-entity";
  private final SilkUtil util;

  public SilkSpawnerCheck() {

    this.util = SilkUtil.hookIntoSilkSpanwers();
  }

  /**
   * Check if this check applies to the specified ItemStack
   *
   * @param stack the ItemStack to check
   *
   * @return true if the check applies to the ItemStack, otherwise false
   */
  @Override
  public boolean applies(final @Nullable ItemStack stack) {

    if(stack == null) {
      return false;
    }

    return util.getStoredEggEntityID(stack) != null || util.getStoredSpawnerItemEntityID(stack) != null;
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

    final String originalCreature = creatureString(stack);
    final String compareCreature = creatureString(compare);

    if(originalCreature == null) {

      return compareCreature == null;
    }

    if(compareCreature == null) {

      return false;
    }

    return originalCreature.equals(compareCreature);
  }

  @Nullable
  public String creatureString(final @Nullable ItemStack stack) {

    if(stack == null) {

      return null;
    }

    final String entityID = util.getStoredEggEntityID(stack);
    if(entityID != null) {
      return entityID;
    }

    return util.getStoredSpawnerItemEntityID(stack);
  }
}