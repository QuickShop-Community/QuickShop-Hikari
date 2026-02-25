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

/**
 * Provides a container for managing tax rates applied to both the interactor
 * and the owner during a transaction in the shop.
 *
 * This class stores and manages the tax rates for the interactor (e.g., the user
 * interacting with the shop) and the owner (e.g., the owner of the shop).
 * It includes methods to retrieve and modify these rates as well as to check
 * if taxes are applied to either party.
 *
 * Instances of this class are primarily used to encapsulate tax rates when
 * calculating taxes using a {@link TaxProvider}.
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class TaxRates {

  private double interactorRate;
  private double shopRate;

  public TaxRates(final double interactorRate, final double shopRate) {

    this.interactorRate = interactorRate;
    this.shopRate = shopRate;
  }

  /**
   * Checks whether a tax rate is applied to the interactor.
   *
   * @return true if the interactor tax rate is not 0.0, false otherwise
   * @since 6.2.0.11
   */
  public boolean taxInteractor() {
    return interactorRate != 0.0;
  }

  /**
   * Checks whether a tax rate is applied to the shop.
   *
   * @return true if the shop tax rate is not 0.0, false otherwise
   * @since 6.2.0.11
   */
  public boolean taxShop() {
    return shopRate != 0.0;
  }

  public double interactorRate() {

    return interactorRate;
  }

  public void interactorRate(final double interactorRate) {

    this.interactorRate = interactorRate;
  }

  public double shopRate() {

    return shopRate;
  }

  public void shopRate(final double shopRate) {

    this.shopRate = shopRate;
  }
}