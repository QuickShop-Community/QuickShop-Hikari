package com.ghostchu.quickshop.api.shop.state;

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

/**
 * ShopState
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopState {

  /**
   * Stable string id, e.g. "active", "frozen", "disabled".
   */
  String identifier();

  /**
   * Translation key for displaying the state name.
   */
  String translationKey();

  /**
   * Provides the translation key used for the miniature lore text associated
   * with this shop state. This text typically represents a brief description
   * or summary of the shop's state for display purposes.
   *
   * @return the translation key for the miniature lore associated with the shop state.
   */
  String miniLoreTranslationKey();

  default boolean overrideShopTypeText() {
    return false;
  }

  /**
   * Whether players are allowed to trade with the shop in this state.
   */
  default boolean isTradingAllowed() {
    return true;
  }

  /**
   * Whether this state should be treated as temporarily blocked.
   * Useful for UX such as showing a paused/frozen icon.
   */
  default boolean isFrozen() {
    return false;
  }

  /**
   * Translation key to explain why trading is unavailable.
   * Null if trading is allowed.
   */
  default String blockedReasonTranslationKey() {
    return null;
  }
}