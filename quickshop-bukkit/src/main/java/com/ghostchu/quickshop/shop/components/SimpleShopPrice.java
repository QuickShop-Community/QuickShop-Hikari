package com.ghostchu.quickshop.shop.components;

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
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.event.settings.type.ShopCurrencyEvent;
import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopPriceBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.service.result.ShopChangeType;
import com.ghostchu.quickshop.shop.builder.SimpleShopPriceBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

/**
 * SimpleShopPrice
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
@SuppressWarnings({ "removal"})
public class SimpleShopPrice implements ShopPrice<Double> {

  private final ModernShop<?, ?, ?, ?> shop;

  @Nullable
  protected String currency;
  protected double price;

  public SimpleShopPrice(@NotNull final ModernShop<?, ?, ?, ?> shop) {
    this.shop = shop;
  }

  public SimpleShopPrice(@NotNull final ModernShop<?, ?, ?, ?> shop,
                         @Nullable final String currency, final double price) {

    this.shop = shop;
    this.currency = currency;
    this.price = price;
  }

  /**
   * Retrieves the price of the shop.
   *
   * @return the price of the shop as an instance of type U, where U represents a generic type.
   */
  @Override
  public Double price() {

    return price;
  }

  /**
   * Sets the price for a shop.
   *
   * @param price the price to set for the shop; must be of type U and should not be null
   */
  @Override
  public void price(final Double price) {

    this.price = price;

    //TODO: Determine how to mark as dirty. Maybe through shop service?
    //setDirty();
    //setSignText();
  }

  /**
   * Gets the currency that shop use
   *
   * @return The currency name
   */
  @Override
  public @Nullable String getCurrency() {

    final ShopCurrencyEvent event = ShopCurrencyEvent.RETRIEVE(this.shop, this.currency);
    event.callEvent();

    return event.updated();
  }

  /**
   * Sets the currency that shop use
   *
   * @param currency The currency name; null to use default currency
   */
  @Override
  public void setCurrency(@Nullable final String currency) {

    if(Objects.equals(this.currency, currency)) {
      return;
    }
    this.currency = currency;
  }

  /**
   * Check if this shop is free shop
   *
   * @return Free Shop
   */
  @Override
  public boolean isFreeShop() {

    return price == 0.0d;
  }

  /**
   * Formats a string representation based on the provided world and optional currency.
   *
   * @param world    the name of the world for which the string is being formatted; must not be
   *                 null
   * @param currency the optional currency to include in the formatted string; can be null
   *
   * @return a formatted string combining the world and currency information; never null
   */
  @Override
  public @NotNull String format(final @NotNull String world, final @Nullable String currency) {


    return QuickShop.getInstance().getEconomyManager().provider().format(BigDecimal.valueOf(price()), world, currency);
  }

  /**
   * Formats a string representation based on the provided world, optional currency, and quantity.
   *
   * @param world    the name of the world for which the string is being formatted; must not be
   *                 null
   * @param currency the optional currency to include in the formatted string; can be null
   * @param quantity the quantity to include in the formatted string; represents a non-negative
   *                 integer
   *
   * @return a formatted string combining the world, currency, and quantity information; never null
   */
  @Override
  public @NotNull String format(final @NotNull String world, final @Nullable String currency, final int quantity) {


    return QuickShop.getInstance().getEconomyManager().provider().format(BigDecimal.valueOf(price() * quantity), world, currency);
  }

  /**
   * Provides a comparator for comparing instances of the generic type U used in the shop's
   * pricing.
   *
   * @return a {@link Comparator} for comparing values of type U
   */
  @Override
  public Comparator<Double> priceComparator() {

    return Comparator.comparingDouble(Double::doubleValue);
  }

  /**
   * Retrieves the maximum number of items that can currently be purchased or acquired based on the
   * shop's available balance and the price of the items.
   *
   * @return the maximum number of items that can be afforded; always a non-negative integer.
   */
  @Override
  public int getMaxAffordable() {
    if(shop.meta().isUnlimited() || this.isFreeShop()) {
      return Integer.MAX_VALUE;
    }

    final BigDecimal unitPrice = BigDecimal.valueOf(this.price());
    if(unitPrice == null || unitPrice.compareTo(ZERO) <= 0) {

      return 0;
    }

    final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();
    if(eco == null) {

      return 0;
    }

    final BigDecimal balance = eco.balance(shop.meta().getOwner(), shop.bukkitLocation().getWorld().getName(), currency);

    if(balance == null || balance.compareTo(ZERO) <= 0) {

      return 0;
    }

    final BigDecimal affordable = balance.divideToIntegralValue(unitPrice);
    if(affordable.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
      return Integer.MAX_VALUE;
    }
    return Math.max(0, affordable.intValue());
  }

  /**
   * Determines whether the current shop can afford the transaction of a specified quantity of
   * items.
   *
   * @param itemAmount the number of items involved in the transaction; must be a non-negative
   *                   integer
   *
   * @return true if the shop can afford the specified number of items, false otherwise
   */
  @Override
  public boolean canAfford(final int itemAmount) {
    if(itemAmount <= 0) {
      return false;
    }
    if(shop.meta().isUnlimited() || this.isFreeShop()) {
      return true;
    }

    final BigDecimal unitPrice = BigDecimal.valueOf(this.price());
    if(unitPrice == null || unitPrice.compareTo(ZERO) <= 0) {
      return false;
    }

    final EconomyProvider eco = QuickShop.getInstance().getEconomyManager().provider();
    if(eco == null) {
      return false;
    }

    final BigDecimal balance = eco.balance(shop.meta().getOwner(), shop.bukkitLocation().getWorld().getName(), currency);
    if(balance == null || balance.compareTo(ZERO) <= 0) {
      return false;
    }

    final BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(itemAmount));
    return balance.compareTo(total) >= 0;
  }

  @Override
  public EnumSet<ShopChangeType> diff(final @Nullable ShopPrice<?> compare) {

    final EnumSet<ShopChangeType> changes = EnumSet.noneOf(ShopChangeType.class);

    if(compare == null || !Objects.equals(this.currency, compare.getCurrency())) {
      changes.add(ShopChangeType.CURRENCY);
    }

    if(compare == null || compare.price() instanceof Double && this.price != (Double)compare.price()) {
      changes.add(ShopChangeType.PRICE);
    }
    return changes;
  }

  @Override
  public ShopPriceBuilder<Double> builder() {

    return new SimpleShopPriceBuilder().price(this.price).currency(this.currency);
  }

  @Override
  public boolean equals(final Object o) {

    if(!(o instanceof final SimpleShopPrice that)) return false;
    return Double.compare(price, that.price) == 0 && Objects.equals(currency, that.currency);
  }

  @Override
  public int hashCode() {

    return Objects.hash(currency, price);
  }
}