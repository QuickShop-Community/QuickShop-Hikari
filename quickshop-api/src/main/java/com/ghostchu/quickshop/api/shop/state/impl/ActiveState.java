package com.ghostchu.quickshop.api.shop.state.impl;

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

import com.ghostchu.quickshop.api.shop.state.ShopState;

/**
 * ActiveState
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ActiveState implements ShopState {

  /**
   * Stable string id, e.g. "active", "frozen", "disabled".
   */
  @Override
  public String identifier() {

    return "active";
  }

  @Override
  public String translationKey() {
    return "shop.state.active";
  }

  /**
   * Provides the translation key used for the miniature lore text associated with this shop state.
   * This text typically represents a brief description or summary of the shop's state for display
   * purposes.
   *
   * @return the translation key for the miniature lore associated with the shop state.
   */
  @Override
  public String miniLoreTranslationKey() {

    return "menu.this-shop-is-active";
  }

  @Override
  public boolean overrideShopTypeText() {
    return false;
  }
}