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
import com.ghostchu.quickshop.api.shop.Shop;

/**
 * BuyingType
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class BuyingType implements IShopType {

  @Override
  public int id() {

    return 1;
  }

  @Override
  public String identifier() {

    return "BUYING";
  }

  @Override
  public String translationKey() {

    return "shop-type.buying";
  }

  @Override
  public String tradingTranslationKey() {

    return "signs.buying";
  }

  @Override
  public String stackTradingTranslationKey() {

    return "signs.stack-buying";
  }

  @Override
  public String outOfStockTranslationKey() {

    return "signs.out-of-space";
  }

  /**
   * Retrieves the translation key associated with the "trading blocked" state of the shop.
   *
   * @return a String representing the translation key for the "trading blocked" state.
   */
  @Override
  public String tradingBlockedTranslationKey() {

    return "";
  }

  /**
   * Retrieves the operation type associated with the shop.
   *
   * @return a {@link ShopOperationEnum} representing the type of operation performed by the shop.
   */
  @Override
  public ShopOperationEnum operationType() {

    return ShopOperationEnum.PURCHASE_BUYING_SHOP;
  }

  /**
   * Determines whether this shop type represents a buying shop.
   *
   * @return true if the shop type is a buying shop, false otherwise.
   */
  @Override
  public boolean isBuying() {

    return true;
  }

  /**
   * Calculates the remaining stock for the given shop.
   *
   * @param shop the shop for which the remaining stock is to be calculated
   *
   * @return the remaining stock as an Integer, or null if it cannot be determined
   */
  @Override
  public Integer remainingStock(final Shop shop) {

    return shop.getRemainingSpace();
  }
}