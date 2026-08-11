package com.ghostchu.quickshop.api.shop;

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

import java.util.concurrent.CompletableFuture;

/**
 * IShopType
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface IShopType {

  /**
   * Retrieves the unique numeric identifier associated with the shop type.
   *
   * @return an integer representing the unique numeric identifier of the shop type.
   */
  int id();

  /**
   * Retrieves the unique identifier associated with the shop type.
   *
   * @return a String representing the unique identifier of the shop type.
   */
  String identifier();

  /**
   * Retrieves the translation key associated with the shop type.
   *
   * @return a String representing the shop type's translation key.
   */
  String translationKey();

  /**
   * Retrieves the translation key associated with the trading functionality
   * in the shop.
   *
   * @return a String representing the translation key for trading.
   */
  String tradingTranslationKey();

  /**
   * Retrieves the translation key associated with stack trading functionality in the shop.
   *
   * @return a String representing the translation key for stack trading
   */
  String stackTradingTranslationKey();

  /**
   * Retrieves the translation key associated with the "out of stock" message for the shop.
   *
   * @return a String representing the translation key for the "out of stock" message
   */
  String outOfStockTranslationKey();

  /**
   * Retrieves the translation key associated with the mini lore functionality of the shop.
   *
   * @return a String representing the translation key for the mini lore functionality.
   */
  String miniLoreTranslationKey();

  /**
   * Retrieves the translation key associated with the "trading blocked" state of the shop.
   *
   * @return a String representing the translation key for the "trading blocked" state.
   */
  String tradingBlockedTranslationKey();

  /**
   * Retrieves the operation type associated with the shop.
   *
   * @return a {@link ShopOperationEnum} representing the type of operation performed by the shop.
   */
  ShopOperationEnum operationType();

  /**
   * Determines whether this shop type represents a buying shop.
   *
   * @return true if the shop type is a buying shop, false otherwise.
   */
  default boolean isBuying() {
    return false;
  }

  /**
   * Determines whether the shop type supports stacking.
   *
   * @return true if the shop type is stackable, false otherwise.
   */
  default boolean isStackable() {
    return true;
  }

  /**
   * Checks if trading is blocked for the shop.
   *
   * @return a boolean indicating whether trading is blocked (true) or not (false).
   */
  default boolean isTradingBlocked() {
    return false;
  }

  /**
   * Calculates the remaining stock for the given shop.
   *
   * @param shop the shop for which the remaining stock is to be calculated
   * @return the remaining stock as an Integer, or null if it cannot be determined
   */
  default Integer remainingStock(final Shop shop) {
    return 0;
  }

  default CompletableFuture<Integer> remainingStockAsync(final Shop shop) {

    return CompletableFuture.completedFuture(remainingStock(shop));
  }
}