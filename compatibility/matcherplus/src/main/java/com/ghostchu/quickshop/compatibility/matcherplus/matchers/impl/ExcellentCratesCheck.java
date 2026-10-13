package com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl;

import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import su.nightexpress.engine.id.Identifier;
import su.nightexpress.excellentcrates.api.ExcellentCratesAPI;
import su.nightexpress.excellentcrates.api.key.KeysAPI;

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

    final KeysAPI keys = getKeys();
    return keys != null && stack != null && keys.getItemAPI().isKeyItem(stack);
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

    final Identifier originalKey = getKeyId(stack);
    final Identifier compareKey = getKeyId(compare);

    if(originalKey == null) {

      return compareKey == null;
    }

    if(compareKey == null) {

      return false;
    }

    return originalKey.equals(compareKey);
  }

  private @Nullable KeysAPI getKeys() {

    final ExcellentCratesAPI api = Bukkit.getServicesManager().load(ExcellentCratesAPI.class);
    return (api == null)? null : api.getKeysAPI().orElse(null);
  }

  private @Nullable Identifier getKeyId(final @Nullable ItemStack stack) {

    final KeysAPI keys = getKeys();
    return (keys == null || stack == null)? null : keys.getItemAPI().getKeyIdFromItem(stack);
  }
}
