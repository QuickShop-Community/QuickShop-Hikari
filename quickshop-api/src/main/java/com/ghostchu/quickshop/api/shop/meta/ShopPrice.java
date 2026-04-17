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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * ShopPrice
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopPrice<U> {

  /**
   * Retrieves the price of the shop.
   *
   * @return the price of the shop as an instance of type U, where U represents a generic type.
   */
  U price();

  /**
   * Sets the price for a shop.
   *
   * @param price the price to set for the shop; must be of type U and should not be null
   */
  void price(U price);

  /**
   * Formats a string representation based on the provided world and optional currency.
   *
   * @param world the name of the world for which the string is being formatted; must not be null
   * @param currency the optional currency to include in the formatted string; can be null
   * @return a formatted string combining the world and currency information; never null
   */
  @NotNull
  String format(final @NotNull String world, final @Nullable String currency);

  /**
   * Formats a string representation based on the provided world, optional currency, and quantity.
   *
   * @param world the name of the world for which the string is being formatted; must not be null
   * @param currency the optional currency to include in the formatted string; can be null
   * @param quantity the quantity to include in the formatted string; represents a non-negative integer
   * @return a formatted string combining the world, currency, and quantity information; never null
   */
  @NotNull
  String format(final @NotNull String world, final @Nullable String currency, final int quantity);

  /**
   * Provides a comparator for comparing instances of the generic type U used in the shop's pricing.
   *
   * @return a {@link Comparator} for comparing values of type U
   */
  Comparator<U> priceComparator();

  /**
   * Compares the price of the current shop with the price of another shop.
   *
   * @param other the other {@code ShopPrice<U>} instance to compare with; must not be null
   * @param reversed whether the comparison should be reversed (i.e., descending order)
   * @return a negative integer, zero, or a positive integer as the price of this shop
   *         is less than, equal to, or greater than the price of the other shop
   */
  default int comparePrice(final U other, final boolean reversed) {

    if(reversed) {
      return priceComparator().reversed().compare(this.price(), other);
    }
    return priceComparator().compare(this.price(), other);
  }

  /**
   * Retrieves the maximum number of items that can currently be purchased or acquired
   * based on the shop's available balance and the price of the items.
   *
   * @return the maximum number of items that can be afforded; always a non-negative integer.
   */
  int getMaxAffordable();

  /**
   * Determines whether the current shop can afford the transaction of a specified quantity of items.
   *
   * @param itemAmount the number of items involved in the transaction; must be a non-negative integer
   * @return true if the shop can afford the specified number of items, false otherwise
   */
  boolean canAfford(final int itemAmount);
}