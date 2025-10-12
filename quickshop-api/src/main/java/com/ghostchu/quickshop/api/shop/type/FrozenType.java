package com.ghostchu.quickshop.api.shop.type;


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

import com.ghostchu.quickshop.api.database.ShopOperationEnum;
import com.ghostchu.quickshop.api.shop.IShopType;

/**
 * FrozenType
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class FrozenType implements IShopType {

  @Override
  public int id() {

    return 2;
  }

  @Override
  public String identifier() {

    return "FROZEN";
  }

  @Override
  public String translationKey() {

    return "shop-type.frozen";
  }

  @Override
  public String tradingTranslationKey() {

    return "signs.freeze";
  }

  @Override
  public String stackTradingTranslationKey() {

    return "signs.freeze";
  }

  @Override
  public String outOfStockTranslationKey() {

    return "signs.freeze";
  }

  /**
   * Retrieves the translation key associated with the "trading blocked" state of the shop.
   *
   * @return a String representing the translation key for the "trading blocked" state.
   */
  @Override
  public String tradingBlockedTranslationKey() {

    return "shop-cannot-trade-when-freezing";
  }

  /**
   * Retrieves the operation type associated with the shop.
   *
   * @return a {@link ShopOperationEnum} representing the type of operation performed by the shop.
   */
  @Override
  public ShopOperationEnum operationType() {

    return ShopOperationEnum.FROZEN;
  }

  /**
   * Checks if trading is blocked for the shop.
   *
   * @return a boolean indicating whether trading is blocked (true) or not (false).
   */
  @Override
  public boolean isTradingBlocked() {

    return true;
  }
}
