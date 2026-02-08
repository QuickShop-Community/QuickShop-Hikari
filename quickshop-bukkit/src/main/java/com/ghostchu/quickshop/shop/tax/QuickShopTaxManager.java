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
import com.ghostchu.quickshop.api.shop.tax.TaxManager;
import com.ghostchu.quickshop.api.shop.tax.TaxProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * QuickShopTaxManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QuickShopTaxManager implements TaxManager {

  protected final Map<String, TaxProvider> providers = new HashMap<>();

  protected String defaultProvider;
  protected String taxAccount;
  protected boolean showTax;
  protected boolean taxUnlimited;
  protected String appliesTo;

  public QuickShopTaxManager() {

    this.defaultProvider = QuickShop.getInstance().getConfig().getString("shop-tax.type", "basic").toLowerCase(Locale.ROOT);
    this.taxAccount = QuickShop.getInstance().getConfig().getString("shop-tax.account", "tax");
    this.showTax = QuickShop.getInstance().getConfig().getBoolean("shop-tax.show", true);
    this.taxUnlimited = !QuickShop.getInstance().getConfig().getBoolean("shop-tax.disable-for-unlimited-shop", false);
    this.appliesTo = QuickShop.getInstance().getConfig().getString("shop-tax.apply-to", "shop");

    provider(new BasicProvider(appliesTo));
    provider(new ProgressiveProvider(appliesTo));
  }

  /**
   * Retrieves a map of all registered tax providers, keyed by their unique identifiers.
   *
   * @return a map where the keys are unique identifiers of tax providers and the values are the
   * corresponding {@link TaxProvider} instances
   *
   * @since 6.2.0.11
   */
  @Override
  public Map<String, TaxProvider> taxProviders() {

    return providers;
  }

  /**
   * Retrieves the identifier of the tax account associated with the tax system.
   *
   * @return a string representing the identifier of the tax account
   *
   * @since 6.2.0.11
   */
  @Override
  public String taxAccount() {

    return taxAccount;
  }

  /**
   * Determines whether tax-related information should be displayed.
   *
   * @return true if tax information should be displayed, false otherwise
   *
   * @since 6.2.0.11
   */
  @Override
  public boolean showTax() {

    return showTax;
  }

  /**
   * Determines whether unlimited shop transactions are subject to tax.
   *
   * @return true if unlimited transactions are taxed, false otherwise
   *
   * @since 6.2.0.11
   */
  @Override
  public boolean taxUnlimited() {

    return taxUnlimited;
  }

  /**
   * Indicates to which entity or entities the tax applies. This could specify whether the tax is
   * applicable to the player, the shop, or both.
   *
   * @return a string representing the entity or entities the tax applies to
   *
   * @since 6.2.0.11
   */
  @Override
  public String appliesTo() {

    return appliesTo;
  }

  /**
   * Registers a new tax provider for handling tax-related calculations.
   *
   * @param provider the {@link TaxProvider} instance to be registered
   *
   * @since 6.2.0.11
   */
  @Override
  public void provider(final TaxProvider provider) {
    this.providers.put(provider.identifier().toLowerCase(Locale.ROOT), provider);
  }

  /**
   * Retrieves the {@link TaxProvider} associated with the given identifier.
   *
   * @param identifier the unique identifier of the tax provider
   *
   * @return the {@link TaxProvider} associated with the specified identifier, or null if no
   * provider exists for the identifier
   *
   * @since 6.2.0.11
   */
  @Override
  public TaxProvider provider(final String identifier) {

    return providers.get(identifier.toLowerCase(Locale.ROOT));
  }

  /**
   * Retrieves the default {@link TaxProvider}.
   *
   * @return the default {@link TaxProvider}, or null if no default provider is set
   *
   * @since 6.2.0.11
   */
  @Override
  public TaxProvider provider() {

    return providers.get(defaultProvider);
  }

  public void defaultProvider(final String defaultProvider) {

    this.defaultProvider = defaultProvider.toLowerCase(Locale.ROOT);
  }
}