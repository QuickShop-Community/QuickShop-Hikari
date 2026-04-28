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
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * ProgressiveProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class ProgressiveProvider implements TaxProvider {

  protected final TreeMap<BigDecimal, Double> taxRates = new TreeMap<>();
  private final String appliesTo;

  public ProgressiveProvider(final String appliesTo) {
    this.appliesTo = appliesTo;

    loadRates();
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

    return "progressive";
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
                                   || appliesTo.equalsIgnoreCase("payee") && shop.isBuying()
                                   || appliesTo.equalsIgnoreCase("both"))? normalizeRate(shop, player, getRate(player, shop.bukkitLocation().getWorld().getName(), shop.getCurrency())) : 0.0;
    final double ownerRate = (appliesTo.equalsIgnoreCase("shop")
                              || appliesTo.equalsIgnoreCase("payee") && shop.isSelling()
                              || appliesTo.equalsIgnoreCase("both"))? normalizeRate(shop, shop.getOwner(), getRate(shop.getOwner(), shop.bukkitLocation().getWorld().getName(), shop.getCurrency())) : 0.0;

    return new TaxRates(interactorRate, ownerRate);
  }

  private Double getRate(final QUser user, final String world, final String currency) {

    if(taxRates.isEmpty()) {
      return 0.0;
    }

    final BigDecimal balance = QuickShop.getInstance().getEconomyManager().provider().balance(user, world, currency);

    final Map.Entry<BigDecimal, Double> entry = taxRates.ceilingEntry(balance);
    if(entry != null) {
      return entry.getValue();
    }

    return taxRates.lastEntry().getValue();
  }

  private double normalizeRate(final Shop shop, final QUser user, final double rate) {

    if(QuickShop.getInstance().perm().hasPermission(user, "quickshop.tax")) {
      Log.debug("Disable the Tax for player " + user + " cause they have permission quickshop.tax");
      return 0.0;
    }

    if(shop.isUnlimited() && QuickShop.getInstance().getConfig().getBoolean("shop-tax.disable-for-unlimited-shop", false)) {
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

  private void loadRates() {

    final String route = "shop-tax.progressive.brackets";

    final Section section = QuickShop.getInstance().getConfig().getSection(route);
    if(section == null) {

      return;
    }

    for(final String key : section.getRoutesAsStrings(false)) {

      try {

        if(key.isEmpty()) continue;

        if(key.equalsIgnoreCase("-1")) {
          taxRates.put(new BigDecimal(Long.MAX_VALUE), section.getDouble(key));
          continue;
        }

        final BigDecimal upperBalance = new BigDecimal(key);
        taxRates.put(upperBalance, section.getDouble(key));

      } catch(final Exception ignore) {

      }
    }
  }
}