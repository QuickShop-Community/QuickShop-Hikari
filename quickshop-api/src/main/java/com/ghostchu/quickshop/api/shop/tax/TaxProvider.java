package com.ghostchu.quickshop.api.shop.tax;

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

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;

/**
 * TaxProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface TaxProvider {

  /**
   * Retrieves the unique identifier of this tax provider.
   *
   * @return a string representing the unique identifier of the tax provider
   * @since 6.2.0.11
   */
  String identifier();

  /**
   * Calculates the applicable tax rates for a transaction based on the given shop
   * and the user initiating the interaction.
   *
   * @param shop   the shop where the transaction is taking place
   * @param player the user initiating the interaction with the shop
   * @return the tax rates applicable to the interactor and the owner of the shop
   * @since 6.2.0.11
   */
  TaxRates calculateTax(Shop shop, QUser player);
}