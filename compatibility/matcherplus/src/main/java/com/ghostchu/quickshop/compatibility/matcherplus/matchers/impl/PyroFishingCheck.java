package com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

/**
 * PyroFishingCheck
 *
 * @author creatorfromhell
 * @since 6.2.0.10
 */
public class PyroFishingCheck implements ItemCheck {

  final NamespacedKey fishNumberKey = new NamespacedKey("pyrofishingpro", "fishnumber");
  final NamespacedKey tierKey = new NamespacedKey("pyrofishingpro", "tier");

  /**
   * Check if this check applies to the specified ItemStack
   *
   * @param stack the ItemStack to check
   *
   * @return true if the check applies to the ItemStack, otherwise false
   */
  @Override
  public boolean applies(final @Nullable ItemStack stack) {

    if(stack == null || stack.getItemMeta() == null) {

      return false;
    }
    return stack.getItemMeta().getPersistentDataContainer().has(fishNumberKey, PersistentDataType.INTEGER)
           && stack.getItemMeta().getPersistentDataContainer().has(tierKey, PersistentDataType.STRING);
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

    final int originalFishNumber = fishNumber(stack);
    final int testerFishNumber = fishNumber(compare);

    final String originalFishTier = fishTier(stack);
    final String testerFishTier = fishTier(compare);

    return originalFishNumber == testerFishNumber && originalFishTier.equalsIgnoreCase(testerFishTier);
  }

  public Integer fishNumber(final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return stack.getItemMeta().getPersistentDataContainer().getOrDefault(fishNumberKey, PersistentDataType.INTEGER, -1);
    }
    return -1;
  }

  public String fishTier(final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return stack.getItemMeta().getPersistentDataContainer().getOrDefault(tierKey, PersistentDataType.STRING, "NO_TIER");
    }
    return "NO_TIER";
  }
}