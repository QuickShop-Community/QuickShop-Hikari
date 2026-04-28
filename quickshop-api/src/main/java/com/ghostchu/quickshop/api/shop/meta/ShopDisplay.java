package com.ghostchu.quickshop.api.shop.meta;

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
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ShopDisplay
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopDisplay {

  /**
   * Check the display location, and teleport, respawn if needs.
   */
  void checkDisplay();

  /**
   * Claim a sign as shop sign (modern method)
   *
   * @param sign The shop sign
   */
  void claimShopSign(@NotNull Sign sign);

  /**
   * Get sign texts on shop's sign.
   *
   * @param locale The locale to be created for
   *
   * @return String arrays represents sign texts: Index | Content Line 0: Header Line 1: Shop Type
   * Line 2: Shop Item Name Line 3: Price
   */
  default List<Component> getSignText(@NotNull final ProxiedLocale locale) {
    //backward support
    throw new UnsupportedOperationException();
  }

  /**
   * Get shop signs, may have multi signs
   *
   * @return Signs for the shop
   */
  @NotNull
  List<Sign> getSigns();

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

  /**
   * Checks if a Sign is a ShopSign
   *
   * @param sign Target {@link Sign}
   *
   * @return Is shop info sign
   */
  boolean isShopSign(@NotNull Sign sign);

  /**
   * Generate new sign texts on shop's sign.
   */
  void setSignText();

  /**
   * Set texts on shop's sign
   *
   * @param paramArrayOfString The texts you want set
   */
  void setSignText(@NotNull List<Component> paramArrayOfString);

  void setSignText(@NotNull ProxiedLocale locale);
}