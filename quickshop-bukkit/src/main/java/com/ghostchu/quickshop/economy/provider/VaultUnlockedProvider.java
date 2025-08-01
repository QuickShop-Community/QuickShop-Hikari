package com.ghostchu.quickshop.economy.provider;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.economyformatter.BuiltInEconomyFormatter;
import com.ghostchu.quickshop.util.logger.Log;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * VaultUnlockedProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class VaultUnlockedProvider implements EconomyProvider, Listener {

  private final String name = "QuickShop-Hikari";
  private final BuiltInEconomyFormatter formatter;
  private String lastError = "No transaction error logged";
  @Nullable
  private net.milkbowl.vault2.economy.Economy economy;

  public VaultUnlockedProvider(@NotNull final QuickShop plugin) {

    super();
    this.formatter = new BuiltInEconomyFormatter(plugin);
    setup();
  }

  private boolean setup() {

    if(!CommonUtil.isClassAvailable("net.milkbowl.vault2.economy.Economy")) {
      return false; // QUICKSHOP-YS I can't believe it broken almost a year and nobody found it, my sentry exploded.
    }

    final RegisteredServiceProvider<net.milkbowl.vault2.economy.Economy> economyProvider;
    try {
      economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault2.economy.Economy.class);

    } catch(final Exception ignore) {

      return false;
    }

    if(economyProvider == null) {
      return false;
    }

    this.economy = economyProvider.getProvider();

    if(this.economy.getName().isEmpty()) {
      QuickShop.getInstance().logger()
              .warn("Current economy provider doesn't correctly implement VaultUnlocked. Please report this to the developer, or use another economy plugin.");
      QuickShop.getInstance().logger()
              .warn("This is technical information, please send this to economy plugin author: VaultEconomyProvider.getName() returns null, or is empty String.");
    } else {
      QuickShop.getInstance().logger().info("Using economy system: " + this.economy.getName());
    }

    Bukkit.getPluginManager().registerEvents(this, QuickShop.getInstance().getJavaPlugin());
    Log.debug("Economy service listener was registered.");
    return true;
  }

  /**
   * Retrieves the name associated with this EconomyProvider.
   *
   * @return The name of the EconomyProvider
   */
  @Override
  public @NotNull String name() {

    return "BuiltIn-VaultUnlocked";
  }

  /**
   * Retrieves the name associated with this EconomyProvider.
   *
   * @return The name of the EconomyProvider
   */
  @Override
  public String providerName() {

    if(this.economy == null) {
      return "Provider not found.";
    }

    return this.economy.getName();
  }

  /**
   * Retrieves the last error that occurred within the EconomyProvider.
   *
   * @return The last error message as a String, or an empty String if no error has occurred.
   */
  @Override
  public @NotNull String lastError() {

    return lastError;
  }

  /**
   * Checks if the instance of EconomyProvider is valid.
   *
   * @return true if the EconomyProvider instance is valid, false otherwise
   */
  @Override
  public boolean valid() {

    return this.economy != null;
  }

  /**
   * Determines if the EconomyProvider supports multiple currencies.
   *
   * @return true if the EconomyProvider supports multiple currencies, false otherwise
   */
  @Override
  public boolean multiCurrency() {

    if(economy == null) {
      return false;
    }

    return economy.hasMultiCurrencySupport();
  }

  /**
   * Check if the EconomyProvider supports the given currency in the given world.
   *
   * @param world    the world to check for currency support
   * @param currency the currency to check for support (null for default currency)
   *
   * @return true if the EconomyProvider supports the given currency in the given world, false
   * otherwise
   */
  @Override
  public boolean supportsCurrency(final @NotNull String world, final @Nullable String currency) {

    if(economy == null || currency == null) {
      return false;
    }

    return this.economy.hasCurrency(currency);
  }

  /**
   * Formats the given amount in the specified world and currency.
   *
   * @param amount   the amount to format
   * @param world    the world in which the balance exists
   * @param currency the currency in which the balance should be formatted (nullable for default
   *                 currency)
   *
   * @return the formatted amount as a String
   */
  @Override
  public @NotNull String format(final @NotNull BigDecimal amount, final @NotNull String world, final @Nullable String currency) {

    if(economy == null) {
      return "No Economy Provider";
    }

    String formatted = null;

    if(currency == null) {
      formatted = this.economy.format(name, amount);
    } else {
      formatted = this.economy.format(name, amount, currency);
    }

    return formatted;
  }

  /**
   * Calculate the balance of a specific user in the given world based on the specified currency.
   *
   * @param user     the user for which to calculate the balance
   * @param world    the world in which the user's balance exists
   * @param currency the currency in which the balance should be calculated (null for default
   *                 currency)
   *
   * @return the balance of the user in the specified currency
   */
  @Override
  public @NotNull BigDecimal balance(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency) {

    if(economy == null || user.getUniqueId() == null) {
      return BigDecimal.ZERO;
    }

    try {

      if(currency == null) {
        return this.economy.balance(name, user.getUniqueId(), world);
      }

      return this.economy.balance(name, user.getUniqueId(), world, currency);
    } catch(final Exception e) {

      if(QuickShop.getInstance().getSentryErrorReporter() != null) {
        QuickShop.getInstance().getSentryErrorReporter().ignoreThrow();
      }

      QuickShop.getInstance().logger().warn("Failure - getBalance - " + name + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return BigDecimal.ZERO;
  }

  /**
   * Deposits the specified amount of currency into the user's account in the specified world.
   *
   * @param user     the user whose account will receive the deposit
   * @param world    the world in which the user's account exists
   * @param currency the currency to be deposited (can be null if default currency is used)
   * @param amount   the amount to be deposited into the user's account
   *
   * @return true if the deposit was successful, false otherwise
   */
  @Override
  public boolean deposit(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {

    if(this.economy == null || user.getUniqueId() == null) {
      return false;
    }

    final EconomyResponse response;
    try {
      if(currency == null) {

        response = this.economy.deposit(name, user.getUniqueId(), world, amount);
      } else {
        response = this.economy.deposit(name, user.getUniqueId(), world, currency, amount);
      }

      if(response.transactionSuccess()) {

        return true;
      }

      this.lastError = providerName() + ": " + response.type.name() + " - " + response.errorMessage;
      Log.transaction(Level.WARNING, "Deposit player " + user.getUniqueId() + " failed, VaultUnlocked response: " + response.errorMessage);
    } catch(final Exception e) {

      QuickShop.getInstance().logger().warn("Failure - deposit - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  /**
   * Withdraws the specified amount of currency from the user's account in the given world.
   *
   * @param user     the user from whose account the amount will be withdrawn
   * @param world    the world in which the user's account exists
   * @param currency the currency in which the amount will be withdrawn (can be null if default
   *                 currency is used)
   * @param amount   the amount to be withdrawn from the user's account
   *
   * @return true if the withdrawal was successful, false otherwise
   */
  @Override
  public boolean withdraw(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {

    if(this.economy == null || user.getUniqueId() == null) {
      return false;
    }

    final EconomyResponse response;
    try {
      if(currency == null) {

        response = this.economy.withdraw(name, user.getUniqueId(), world, amount);
      } else {
        response = this.economy.withdraw(name, user.getUniqueId(), world, currency, amount);
      }

      if(response.transactionSuccess()) {

        return true;
      }

      this.lastError = providerName() + ": " + response.type.name() + " - " + response.errorMessage;
      Log.transaction(Level.WARNING, "Withdraw player " + user.getUniqueId() + " failed, VaultUnlocked response: " + response.errorMessage);
    } catch(final Exception e) {

      QuickShop.getInstance().logger().warn("Failure - withdraw - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  public Economy economy() {

    return economy;
  }

  @EventHandler
  public void onServiceRegister(final ServiceRegisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault2.economy.Economy)) {
      return;
    }
    setup();
  }

  @EventHandler
  public void onServiceUnregister(final ServiceUnregisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault2.economy.Economy)) {
      return;
    }
    setup();
  }
}
