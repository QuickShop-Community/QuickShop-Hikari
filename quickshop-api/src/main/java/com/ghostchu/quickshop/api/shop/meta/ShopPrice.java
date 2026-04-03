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