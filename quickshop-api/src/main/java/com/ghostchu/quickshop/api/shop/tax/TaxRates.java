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
 * Represents the tax rates applicable to interactions between different entities,
 * specifically the interactor and the owner.
 *
 * @param interactorRate the tax rate applied to the interactor
 * @param ownerRate the tax rate applied to the owner
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public record TaxRates(double interactorRate, double ownerRate) {

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
   * Checks whether a tax rate is applied to the owner.
   *
   * @return true if the owner tax rate is not 0.0, false otherwise
   * @since 6.2.0.11
   */
  public boolean taxOwner() {
    return ownerRate != 0.0;
  }
}