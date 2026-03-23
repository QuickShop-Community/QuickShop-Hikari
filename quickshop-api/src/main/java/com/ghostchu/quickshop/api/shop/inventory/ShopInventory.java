package com.ghostchu.quickshop.api.shop.inventory;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ShopInventory
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopInventory {

  /**
   * Add x ItemStack to the shop inventory
   *
   * @param paramItemStack The ItemStack you want add
   * @param paramInt       How many you want add
   */
  void add(@NotNull ItemStack paramItemStack, int paramInt);

  /**
   * Remove x ItemStack from the shop inventory
   *
   * @param paramItemStack Want removed ItemStack
   * @param paramInt       Want remove how many
   */
  void remove(@NotNull ItemStack paramItemStack, int paramInt);

  /**
   * Gets the shop Inventory
   *
   * @return Inventory
   */
  @Nullable
  InventoryWrapper getInventory();

  /**
   * Gets the InventoryWrapper provider name (the plugin name who register it), usually is
   * QuickShop
   *
   * @return InventoryWrapper
   */
  @NotNull
  String getInventoryWrapperProvider();

  /**
   * Get shop remaining space.
   *
   * @return Remaining space.
   */
  int getRemainingSpace();

  /**
   * Get shop remaining stock.
   *
   * @return Remaining stock.
   */
  int getRemainingStock();

  int getShopStackingAmount();

  /**
   * Check if shop out of space or out of stock
   *
   * @return true if out of space or out of stock
   */
  boolean inventoryAvailable();

  /**
   * Check the target ItemStack is matches with this shop's item.
   *
   * @param paramItemStack Target ItemStack.
   *
   * @return Matches
   */
  boolean matches(@NotNull ItemStack paramItemStack);

  void setInventory(@NotNull InventoryWrapper wrapper, @NotNull InventoryWrapperManager manager);
}