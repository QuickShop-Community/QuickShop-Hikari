package com.ghostchu.quickshop.economyrevamp.provider;
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
import com.ghostchu.quickshop.api.economyrevamp.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.economyformatter.BuiltInEconomyFormatter;
import com.ghostchu.quickshop.util.logger.Log;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Level;

/**
 * VaultProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class VaultProvider implements EconomyProvider, Listener {

  private final String name = "QuickShop-Hikari";
  private final BuiltInEconomyFormatter formatter;
  private String lastError = "No transaction error logged";
  @Nullable
  private net.milkbowl.vault.economy.Economy economy;

  public VaultProvider(@NotNull final QuickShop plugin) {

    super();
    this.formatter = new BuiltInEconomyFormatter(plugin);
    setup();
  }

  private boolean setup() {

    if(!CommonUtil.isClassAvailable("net.milkbowl.vault.economy.Economy")) {
      return false; // QUICKSHOP-YS I can't believe it broken almost a year and nobody found it, my sentry exploded.
    }

    final RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider;
    try {
      economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

    } catch(final Exception ignore) {

      return false;
    }

    if(economyProvider == null) {
      return false;
    }

    this.economy = economyProvider.getProvider();

    if(this.economy.getName().isEmpty()) {
      QuickShop.getInstance().logger()
              .warn("Current economy provider doesn't correctly implement Vault. Please report this to the developer, or use another economy plugin.");
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

    return "BuiltIn-Vault";
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

    return false;
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

    return false;
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

    if(!valid()) {
      return "Error";
    }
    try {
      final String formatedBalance = Objects.requireNonNull(this.economy).format(amount.doubleValue());
      if(formatedBalance == null) {
        return this.formatter.getInternalFormat(amount.doubleValue(), null);
      }
      return formatedBalance;
    } catch(final Exception e) {

      return this.formatter.getInternalFormat(amount.doubleValue(), null);
    }
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

    if(!valid()) {
      return BigDecimal.ZERO;
    }

    try {
      return BigDecimal.valueOf(Objects.requireNonNull(this.economy).getBalance(Bukkit.getOfflinePlayer(user.getUniqueId()), world));

    } catch(final Exception t) {
      if(QuickShop.getInstance().getSentryErrorReporter() != null) {
        QuickShop.getInstance().getSentryErrorReporter().ignoreThrow();
      }
      QuickShop.getInstance().logger().warn("Failure - getBalance - " + user + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), t);
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

    if(!valid()) {
      return false;
    }
    try {
      final EconomyResponse response = Objects.requireNonNull(this.economy).depositPlayer(Bukkit.getOfflinePlayer(user.getUniqueId()), world, amount.doubleValue());
      if(response.transactionSuccess()) {
        return true;
      }
      this.lastError = providerName() + ": " + response.type.name() + " - " + response.errorMessage;
      Log.transaction(Level.WARNING, "Deposit player " + user.getUniqueId() + " failed, Vault response: " + response.errorMessage);
      return false;
    } catch(final Exception t) {
      if(QuickShop.getInstance().getSentryErrorReporter() != null) {
        QuickShop.getInstance().getSentryErrorReporter().ignoreThrow();
      }
      if(user.getUsername() == null) {
        QuickShop.getInstance().logger().warn("Deposit failed and player name is NULL, Player uuid: " + user.getUniqueId() + ". Provider (" + providerName() + ")");
        return false;
      }
      QuickShop.getInstance().logger().warn("Failure - deposit - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), t);
      return false;
    }
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

    if(!valid()) {
      return false;
    }
    try {

      if(balance(user, world, currency).compareTo(amount) < 0) {
        return false;
      }

      final EconomyResponse response = Objects.requireNonNull(this.economy).withdrawPlayer(Bukkit.getOfflinePlayer(user.getUniqueId()), world, amount.doubleValue());
      if(response.transactionSuccess()) {
        return true;
      }
      this.lastError = providerName() + ": " + response.type.name() + " - " + response.errorMessage;
      Log.transaction(Level.WARNING, "Withdraw player " + user.getUniqueId() + " failed, Vault response: " + response.errorMessage);
      return false;
    } catch(final Exception t) {

      if(QuickShop.getInstance().getSentryErrorReporter() != null) {
        QuickShop.getInstance().getSentryErrorReporter().ignoreThrow();
      }
      if(user.getUsername() == null) {
        QuickShop.getInstance().logger().warn("Withdraw failed and player name is NULL, Player uuid: {}", user.getUniqueId() + ", Provider: " + providerName());
        return false;
      }
      QuickShop.getInstance().logger().warn("Failure - withdraw - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), t);
      return false;
    }
  }

  public net.milkbowl.vault.economy.Economy economy() {

    return economy;
  }

  @EventHandler
  public void onServiceRegister(final ServiceRegisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
      return;
    }
    setup();
  }

  @EventHandler
  public void onServiceUnregister(final ServiceUnregisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
      return;
    }
    setup();
  }
}