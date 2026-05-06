package com.ghostchu.quickshop.api.shop.builder;

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


import com.ghostchu.quickshop.api.shop.components.ShopItem;
import org.bukkit.inventory.ItemStack;

/**
 * ShopItemBuilder
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopItemBuilder {

  /**
   * Retrieves the {@code ItemStack} associated with the shop item being built.
   *
   * @return the {@code ItemStack} instance representing the item configuration for the shop.
   */
  ItemStack item();

  /**
   * Sets the item to be associated with the shop.
   *
   * @param item the {@code ItemStack} representing the item to set; must not be null
   * @return the current {@code ShopItemBuilder} instance for method chaining
   */
  ShopItemBuilder item(ItemStack item);

  /**
   * Checks whether the display of the shop item is disabled.
   *
   * @return true if the display is disabled, false otherwise.
   */
  boolean isDisableDisplay();

  /**
   * Sets whether the display of the shop item should be disabled.
   *
   * @param disabled a boolean value indicating whether to disable the display;
   *                 {@code true} to disable the display, {@code false} to enable it
   * @return the current {@code ShopItemBuilder} instance for method chaining
   */
  ShopItemBuilder disableDisplay(boolean disabled);

  /**
   * Constructs and returns a new {@link ShopItem} instance based on the current builder configuration.
   *
   * @return a new {@link ShopItem} instance.
   */
  ShopItem build();
}