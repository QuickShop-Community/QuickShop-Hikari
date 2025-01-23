package com.ghostchu.quickshop.util.matcher.item;

/*
 * QuickShop - Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import net.tnemc.item.bukkit.BukkitCalculationsProvider;
import net.tnemc.item.bukkit.BukkitItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TNEItemMatcherImpl
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class TNEItemMatcherImpl implements ItemMatcher {

  final BukkitCalculationsProvider calc = new BukkitCalculationsProvider();

  private final QuickShop plugin;

  public TNEItemMatcherImpl(final QuickShop plugin) {

    this.plugin = plugin;
  }

  /**
   * Gets the ItemMatcher provider name
   *
   * @return Provider name
   */
  @Override
  public @NotNull String getName() {

    return "TNE";
  }

  /**
   * Gets the ItemMatcher provider plugin instance
   *
   * @return Provider Plugin instance
   */
  @Override
  public @NotNull Plugin getPlugin() {

    return this.plugin.getJavaPlugin();
  }

  /**
   * Tests ItemStacks is matches BEWARE: Different order of itemstacks might get different results
   *
   * @param original The original ItemStack
   * @param tester   The ItemStack will test matches with original itemstack.
   *
   * @return The result of tests
   */
  @Override
  public boolean matches(@Nullable final ItemStack original, @Nullable final ItemStack tester) {

    if(original == null || tester == null) {
      return false;
    }

    final int originalFish = fishData(original);
    final int testerFish = fishData(tester);
    if(originalFish > -1 || testerFish > -1) {
      return originalFish == testerFish;
    }

    return calc.itemsEqual(BukkitItemStack.locale(original), BukkitItemStack.locale(tester));
  }

  //TODO: Move this inside TNIL using the new updated code that will support PDC.
  public Integer fishData(final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return stack.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("pyrofishingpro", "fishnumber"), PersistentDataType.INTEGER, -1);
    }
    return -1;
  }
}