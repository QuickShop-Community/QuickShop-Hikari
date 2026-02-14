package com.ghostchu.quickshop.shop.tax;

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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tax.TaxProvider;
import com.ghostchu.quickshop.api.shop.tax.TaxRates;
import com.ghostchu.quickshop.util.logger.Log;

/**
 * BasicProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class BasicProvider implements TaxProvider {

  private final double rate;
  private final String appliesTo;

  public BasicProvider(final String appliesTo) {

    this.rate = QuickShop.getInstance().getConfig().getDouble("shop-tax.basic.rate", 0.05);
    this.appliesTo = appliesTo;
  }

  /**
   * Retrieves the unique identifier of this tax provider.
   *
   * @return a string representing the unique identifier of the tax provider
   *
   * @since 6.2.0.11
   */
  @Override
  public String identifier() {

    return "basic";
  }

  /**
   * Calculates the applicable tax rates for a transaction based on the given shop and the user
   * initiating the interaction.
   *
   * @param shop   the shop where the transaction is taking place
   * @param player the user initiating the interaction with the shop
   *
   * @return the tax rates applicable to the interactor and the owner of the shop
   *
   * @since 6.2.0.11
   */
  @Override
  public TaxRates calculateTax(final Shop shop, final QUser player) {

    final double interactorRate = (appliesTo.equalsIgnoreCase("player")
                                   || appliesTo.equalsIgnoreCase("both"))? normalizeRate(shop, player) : 0.0;
    final double ownerRate = (appliesTo.equalsIgnoreCase("shop")
                              || appliesTo.equalsIgnoreCase("both"))? normalizeRate(shop, shop.getOwner()) : 0.0;

    return new TaxRates(interactorRate, ownerRate);
  }

  private double normalizeRate(final Shop shop, final QUser user) {

    if(QuickShop.getInstance().perm().hasPermission(user, "quickshop.tax")) {
      Log.debug("Disable the Tax for player " + user + " cause they have permission quickshop.tax");
      return 0.0;
    }

    if(shop.isUnlimited() && QuickShop.getInstance().perm().hasPermission(user, "quickshop.tax.bypassunlimited")) {
      Log.debug("Disable the Tax for player " + user + " cause they have permission quickshop.tax.bypassunlimited and shop is unlimited.");
      return 0.0;
    }

    if(rate >= 1.0 || rate < 0.0) {

      Log.debug("Disable tax due to is invalid, it should be in >=0.0 and <1.0 (current value is " + rate + ")");
      return 0.0;
    }
    return rate;
  }
}