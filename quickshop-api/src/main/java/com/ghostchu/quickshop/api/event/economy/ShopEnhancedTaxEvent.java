package com.ghostchu.quickshop.api.event.economy;

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

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tax.TaxRates;
import org.jetbrains.annotations.NotNull;

/**
 * ShopEnhancedTaxEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class ShopEnhancedTaxEvent extends AbstractQSEvent {

  private final QUser user;
  private final Shop shop;
  private TaxRates tax;

  /**
   * Call when shop calc shop tax that will pay to system account and remove from shop owner/player
   * received money
   *
   * @param shop The shop
   * @param tax  The tax
   * @param user The user (buyer/seller)
   */
  public ShopEnhancedTaxEvent(@NotNull final Shop shop, final TaxRates tax, @NotNull final QUser user) {

    this.shop = shop;
    this.tax = tax;
    this.user = user;
  }

  /**
   * Gets the shop
   *
   * @return the shop
   */
  public Shop getShop() {

    return this.shop;
  }

  /**
   * Gets the tax in purchase
   *
   * @return tax
   */
  public TaxRates getTax() {

    return this.tax;
  }

  /**
   * Sets the new tax in purchase
   *
   * @param tax New tax
   */
  public void setTax(final TaxRates tax) {

    this.tax = tax;
  }

  public void setInteractorTax(final double rate) {

    this.tax.interactorRate(rate);
  }

  public void setShopTax(final double rate) {

    this.tax.shopRate(rate);
  }

  /**
   * Gets the user (buyer or seller)
   *
   * @return User
   */
  public QUser getUser() {

    return this.user;
  }
}