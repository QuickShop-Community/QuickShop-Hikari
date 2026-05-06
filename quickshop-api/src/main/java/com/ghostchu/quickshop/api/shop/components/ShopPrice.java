package com.ghostchu.quickshop.api.shop.components;

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

import com.ghostchu.quickshop.api.shop.ShopService;
import com.ghostchu.quickshop.api.shop.builder.ShopPermissionBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopPriceBuilder;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * ShopPrice
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopPrice<T> {

  /**
   * Retrieves the price of the shop.
   *
   * @return the price of the shop as an instance of type U, where U represents a generic type.
   */
  T price();

  /**
   * Sets the price for a shop.
   *
   * @param price the price to set for the shop; must be of type U and should not be null
   *
   * @deprecated All setting operations should go through the {@link ShopService} going forward.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  void price(T price);

  /**
   * Gets the currency that shop use
   *
   * @return The currency name
   */
  @Nullable
  String getCurrency();

  /**
   * Sets the currency that shop use
   *
   * @param currency The currency name; null to use default currency
   *
   * @deprecated All setting operations should go through the {@link ShopService} going forward.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  void setCurrency(@Nullable String currency);

  /**
   * Check if this shop is free shop
   *
   * @return Free Shop
   */
  boolean isFreeShop();

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
  Comparator<T> priceComparator();

  /**
   * Compares the price of the current shop with the price of another shop.
   *
   * @param other the other {@code ShopPrice<U>} instance to compare with; must not be null
   * @param reversed whether the comparison should be reversed (i.e., descending order)
   * @return a negative integer, zero, or a positive integer as the price of this shop
   *         is less than, equal to, or greater than the price of the other shop
   */
  default int comparePrice(final T other, final boolean reversed) {

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

  /**
   * Computes and returns the differences between the current shop and another specified shop based on their properties.
   * The differences are represented as a set of {@code ShopChangeType} enumerations.
   *
   * @param compare The {@code ShopPrice} object to compare against. Can be null, which will indicate
   *                a comparison against the absence of a shop.
   * @return An {@code EnumSet} of {@code ShopChangeType} representing the differences between the
   *         current shop and the provided shop. Returns an empty set if no differences are found.
   */
  EnumSet<ShopChangeType> diff(final @Nullable ShopPrice<?> compare);

  /**
   * Creates and returns a {@link ShopPriceBuilder} instance to customize and build a {@link ShopPrice}.
   *
   * @return a {@link ShopPriceBuilder} to configure and construct a new {@link ShopPrice}.
   */
  ShopPriceBuilder<T> asBuilder();
}