package com.ghostchu.quickshop.api.shop.components;

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

import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.display.DisplayItem;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ShopDisplay
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopItem {

  /**
   * Get shop item's ItemStack
   *
   * @return The shop's ItemStack
   */
  @NotNull
  ItemStack getItem();

  /**
   * Set shop item's ItemStack
   *
   * @param item ItemStack to set
   */
  void setItem(@NotNull ItemStack item);

  /**
   * Encodes and retrieves information related to the shop's item.
   *
   * @return a string representation of the encoded item data
   */
  String encodedItem();

  /**
   * Gets shop status is stacking shop
   *
   * @return The shop stacking status
   */
  boolean isStackingShop();

  /**
   * Getting the item stacking amount of the shop.
   *
   * @return The item stacking amount of the shop.
   */
  int getShopStackingAmount();

  /**
   * Getting if this shop has been disabled the display
   *
   * @return Does display has been disabled
   */
  boolean isDisableDisplay();

  /**
   * Set the display disable state
   *
   * @param disabled Has been disabled
   */
  void setDisableDisplay(boolean disabled);

  /**
   * Get the display item
   *
   * @return The display item
   */
  DisplayItem getDisplayItem();

  /**
   * Determines whether a custom item name should be used.
   *
   * @return true if a custom item name is enabled, false otherwise
   */
  boolean useCustomItemName();

  /**
   * Customizes and returns a Component representing an item name.
   *
   * @return a Component representing the customized item name
   */
  Component customItemName();
}