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

import java.util.concurrent.CompletableFuture;

/**
 * SellingType
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class SellingType implements IShopType {

  @Override
  public int id() {

    return 0;
  }

  @Override
  public String identifier() {

    return "SELLING";
  }

  @Override
  public String translationKey() {

    return "shop-type.selling";
  }

  @Override
  public String tradingTranslationKey() {

    return "signs.selling";
  }

  @Override
  public String stackTradingTranslationKey() {

    return "signs.stack-selling";
  }

  @Override
  public String outOfStockTranslationKey() {

    return "signs.out-of-stock";
  }

  /**
   * Retrieves the translation key associated with the mini lore functionality of the shop.
   *
   * @return a String representing the translation key for the mini lore functionality.
   */
  @Override
  public String miniLoreTranslationKey() {

    return "menu.this-shop-is-selling";
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

    return ShopOperationEnum.PURCHASE_SELLING_SHOP;
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

    return shop.getRemainingStock();
  }

  @Override
  public CompletableFuture<Integer> remainingStockAsync(final Shop shop) {

    return shop.getRemainingStockAsync();
  }
}
