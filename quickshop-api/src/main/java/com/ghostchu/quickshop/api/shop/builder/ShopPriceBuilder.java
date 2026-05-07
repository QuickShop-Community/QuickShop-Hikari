package com.ghostchu.quickshop.api.shop.builder;

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


import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ShopPriceBuilder
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopPriceBuilder<T> {

  /**
   * Retrieves the price configuration associated with the shop being built.
   *
   * @return the price configuration of type {@code T}.
   */
  T price();

  /**
   * Assigns a price to the shop being built.
   *
   * @param price the price to assign to the shop; must not be null
   * @return the current {@code ShopPriceBuilder} instance for method chaining
   */
  ShopPriceBuilder<T> price(@NotNull final T price);

  /**
   * Retrieves the currency identifier associated with the shop price being built.
   * The currency identifier represents the type of currency, such as USD, EUR, etc.
   *
   * @return the currency identifier as a String, or {@code null} if no specific currency is set.
   */
  @Nullable
  String currency();

  /**
   * Sets the currency for the shop price being built.
   *
   * @param currency the currency identifier to assign to the shop price;
   *                 can be null to indicate no specific currency is set.
   * @return the current {@code ShopPriceBuilder} instance for method chaining.
   */
  ShopPriceBuilder<T> currency(@Nullable final String currency);

  /**
   * Builds and returns a new {@link ShopPrice} instance associated with the specified shop.
   *
   * @param shop the {@link ModernShop} instance for which the {@link ShopPrice} is being constructed;
   *             must not be null and should be a valid representation of a shop.
   * @return a new {@link ShopPrice} instance containing the price configuration and currency
   *         settings as defined in the builder for the provided shop.
   */
  ShopPrice<T> build(ModernShop<?, ?, ?, ?> shop);
}