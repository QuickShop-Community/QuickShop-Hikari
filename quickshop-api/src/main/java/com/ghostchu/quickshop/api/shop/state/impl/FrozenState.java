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
 * FrozenState
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class FrozenState implements ShopState {

  /**
   * Stable string id, e.g. "active", "frozen", "disabled".
   */
  @Override
  public String identifier() {

    return "frozen";
  }

  /**
   * Translation key for displaying the state name.
   */
  @Override
  public String translationKey() {

    return "signs.freeze";
  }

  @Override
  public boolean overrideShopTypeText() {
    return true;
  }

  /**
   * Whether players are allowed to trade with the shop in this state.
   */
  @Override
  public boolean isTradingAllowed() {
    return false;
  }

  /**
   * Whether this state should be treated as temporarily blocked.
   * Useful for UX such as showing a paused/frozen icon.
   */
  @Override
  public boolean isFrozen() {
    return true;
  }

  /**
   * Translation key to explain why trading is unavailable.
   * Null if trading is allowed.
   */
  @Override
  public String blockedReasonTranslationKey() {
    return "shop-cannot-trade-when-freezing";
  }
}