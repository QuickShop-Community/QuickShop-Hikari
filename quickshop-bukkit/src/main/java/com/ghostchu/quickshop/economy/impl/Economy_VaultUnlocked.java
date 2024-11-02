package com.ghostchu.quickshop.economy.impl;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.economy.NonSeparateAbstractEconomy;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.economyformatter.BuiltInEconomyFormatter;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault2.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Economy_VaultUnlocked
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class Economy_VaultUnlocked extends NonSeparateAbstractEconomy implements Listener {

  private final String name = "QuickShop-Hikari";

  private final QuickShop plugin;
  private final BuiltInEconomyFormatter formatter;

  private boolean allowLoan;

  @Getter
  @Setter
  @Nullable
  private net.milkbowl.vault2.economy.Economy vault;

  @Nullable
  private String lastError = null;

  public Economy_VaultUnlocked(@NotNull final QuickShop plugin) {

    super();
    this.plugin = plugin;
    this.formatter = new BuiltInEconomyFormatter(plugin);
    plugin.getReloadManager().register(this);
    init();
    setupEconomy();
  }

  private void init() {

    this.allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
  }

  private boolean setupEconomy() {

    if(!CommonUtil.isClassAvailable("net.milkbowl.vault2.economy.Economy")) {
      return false; // QUICKSHOP-YS I can't believe it broken almost a year and nobody found it, my sentry exploded.
    }
    final RegisteredServiceProvider<Economy> economyProvider;
    try {
      economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault2.economy.Economy.class);
    } catch(final Exception e) {
      return false;
    }

    if(economyProvider != null) {
      this.vault = economyProvider.getProvider();
    }

    if(this.vault == null) {
      return false;
    }

    if(this.vault.getName().isEmpty()) {
      plugin
              .logger()
              .warn(
                      "Current economy plugin not correct process all request, this usually cause by irregular code, you should report this issue to your economy plugin author or use other economy plugin.");
      plugin
              .logger()
              .warn(
                      "This is technical information, please send this to economy plugin author: "
                      + "VaultEconomyProvider.getName() return a null or empty.");
    } else {
      plugin.logger().info("Using economy system: " + this.vault.getName());
    }
    Bukkit.getPluginManager().registerEvents(this, plugin.getJavaPlugin());
    Log.debug("Economy service listener was registered.");
    return true;
  }

  /**
   * Deposits a given amount of money from thin air to the given username.
   *
   * @param name     The exact (case insensitive) username to give money to
   * @param amount   The amount to give them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success (Should be almost always)
   */
  @Override
  public boolean deposit(@NotNull final String name, final double amount, @NotNull final World world, @Nullable final String currency) {

    return false;
  }

  /**
   * Deposits a given amount of money from thin air to the given username.
   *
   * @param identifier     The exact (case insensitive) username to give money to
   * @param amount   The amount to give them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success (Should be almost always)
   */
  @Override
  public boolean deposit(@NotNull final UUID identifier, final double amount, @NotNull final World world, @Nullable final String currency) {

    if(vault == null) {
      return false;
    }

    if(currency == null) {

      return vault.deposit(name, identifier, world.getName(), BigDecimal.valueOf(amount)).transactionSuccess();
    }

    return vault.deposit(name, identifier, world.getName(), currency, BigDecimal.valueOf(amount)).transactionSuccess();
  }

  /**
   * Deposits a given amount of money from thin air to the given username.
   *
   * @param trader   The player to give money to
   * @param amount   The amount to give them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success (Should be almost always)
   */
  @Override
  public boolean deposit(@NotNull final OfflinePlayer trader, final double amount, @NotNull final World world, @Nullable final String currency) {

    return deposit(trader.getUniqueId(), amount, world, currency);
  }

  /**
   * Fetches the balance of the given account name
   *
   * @param name     The uuid of the account
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return Their current balance.
   */
  @Override
  public double getBalance(@NotNull final String name, @NotNull final World world, @Nullable final String currency) {

    return 0;
  }

  /**
   * Fetches the balance of the given account name
   *
   * @param identifier     The uuid of the account
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return Their current balance.
   */
  @Override
  public double getBalance(@NotNull final UUID identifier, @NotNull final World world, @Nullable final String currency) {

    if(vault == null) {
      return 0;
    }

    if(currency == null) {

      return vault.getBalance(name, identifier, world.getName()).doubleValue();
    }

    return vault.getBalance(name, identifier, world.getName(), currency).doubleValue();
  }

  /**
   * Fetches the balance of the given player
   *
   * @param player   The name of the account
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return Their current balance.
   */
  @Override
  public double getBalance(@NotNull final OfflinePlayer player, @NotNull final World world, @Nullable final String currency) {

    return getBalance(player.getUniqueId(), world, currency);
  }

  /**
   * Withdraws a given amount of money from the given username and turns it to thin air.
   *
   * @param name     The exact (case insensitive) username to take money from
   * @param amount   The amount to take from them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success, false if they didn't have enough cash
   */
  @Override
  public boolean withdraw(@NotNull final String name, final double amount, @NotNull final World world, @Nullable final String currency) {

    return false;
  }

  /**
   * Withdraws a given amount of money from the given username and turns it to thin air.
   *
   * @param identifier     The exact (case insensitive) username to take money from
   * @param amount   The amount to take from them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success, false if they didn't have enough cash
   */
  @Override
  public boolean withdraw(@NotNull final UUID identifier, final double amount, @NotNull final World world, @Nullable final String currency) {

    if(vault == null) {
      return false;
    }

    if(currency == null) {

      return vault.withdraw(name, identifier, world.getName(), BigDecimal.valueOf(amount)).transactionSuccess();
    }

    return vault.withdraw(name, identifier, world.getName(), currency, BigDecimal.valueOf(amount)).transactionSuccess();
  }

  /**
   * Withdraws a given amount of money from the given username and turns it to thin air.
   *
   * @param trader   The player to take money from
   * @param amount   The amount to take from them
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return True if success, false if they didn't have enough cash
   */
  @Override
  public boolean withdraw(@NotNull final OfflinePlayer trader, final double amount, @NotNull final World world, @Nullable final String currency) {

    return withdraw(trader.getUniqueId(), amount, world, currency);
  }

  @Override
  public String getProviderName() {

    return "VaultUnlocked";
  }

  /**
   * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
   *
   * @param balance  The given number
   * @param world    The transaction world
   * @param currency The currency name
   *
   * @return The balance in human readable text.
   */
  @Override
  public String format(final double balance, @NotNull final World world, @Nullable final String currency) {

    if(vault == null) {
      return "No Economy Plugin";
    }

    if(currency == null) {

      return vault.format(name, BigDecimal.valueOf(balance));
    }

    return vault.format(name, BigDecimal.valueOf(balance), currency);
  }

  /**
   * Gets the economy processor last error message
   *
   * @return Error message or null if never happens
   */
  @Override
  public @Nullable String getLastError() {

    return this.lastError;
  }

  /**
   * Getting Economy impl owned by
   *
   * @return Owned by
   */
  @Override
  public @NotNull Plugin getPlugin() {

    return plugin.getJavaPlugin();
  }

  /**
   * Gets the currency does exists
   *
   * @param world    The transaction world
   * @param currency Currency name
   *
   * @return exists
   */
  @Override
  public boolean hasCurrency(@NotNull final World world, @NotNull final String currency) {

    if(this.vault == null) {
      return false;
    }

    return vault.hasCurrency(currency);
  }

  /**
   * Checks that this economy is valid. Returns false if it is not valid.
   *
   * @return True if this economy will work, false if it will not.
   */
  @Override
  public boolean isValid() {

    return this.vault != null;
  }

  /**
   * Gets currency supports status
   *
   * @return true if supports
   */
  @Override
  public boolean supportCurrency() {

    if(this.vault == null) {
      return false;
    }

    return vault.hasMultiCurrencySupport();
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  @Override
  public String toString() {

    return "What";
  }

  @EventHandler
  public void onServiceRegister(final ServiceRegisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
      return;
    }
    setupEconomy();
  }

  @EventHandler
  public void onServiceUnregister(final ServiceUnregisterEvent event) {

    if(!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
      return;
    }
    setupEconomy();
  }
}