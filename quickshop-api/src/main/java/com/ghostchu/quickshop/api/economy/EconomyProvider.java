package com.ghostchu.quickshop.api.economy;
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

import com.ghostchu.quickshop.api.obj.QUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * EconomyProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface EconomyProvider {

  String ERROR_MESSAGE = "QuickShop received an error when processing Economy response, THIS NOT A QUICKSHOP FAULT, you might need ask help with your Economy Provider plugin (%s) author.";

  /**
   * Retrieves the name associated with this EconomyProvider.
   *
   * @return The name of the EconomyProvider
   */
  @NotNull
  String name();

  /**
   * Retrieves the name associated with this EconomyProvider.
   *
   * @return The name of the EconomyProvider
   */
  String providerName();

  /**
   * Retrieves the last error that occurred within the EconomyProvider.
   *
   * @return The last error message as a String, or an empty String if no error has occurred.
   */
  @NotNull
  String lastError();

  /**
   * Checks if the instance of EconomyProvider is valid.
   *
   * @return true if the EconomyProvider instance is valid, false otherwise
   */
  boolean valid();

  /**
   * Determines if the EconomyProvider supports multiple currencies.
   *
   * @return true if the EconomyProvider supports multiple currencies, false otherwise
   */
  boolean multiCurrency();

  /**
   * Check if the EconomyProvider supports the given currency in the given world.
   *
   * @param world    the world to check for currency support
   * @param currency the currency to check for support (null for default currency)
   *
   * @return true if the EconomyProvider supports the given currency in the given world, false
   * otherwise
   */
  boolean supportsCurrency(final @NotNull String world, final @Nullable String currency);

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
  @NotNull
  String format(final @NotNull BigDecimal amount, final @NotNull String world, final @Nullable String currency);

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
  @NotNull
  BigDecimal balance(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency);

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
  boolean deposit(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount);

  /**
   * Transfers the specified amount of currency from one user to another user in the given world.
   *
   * @param from     the user from whose account the amount will be transferred
   * @param to       the user to whose account the amount will be transferred
   * @param world    the world in which the users' accounts exist
   * @param currency the currency in which the amount will be transferred (can be null if default
   *                 currency is used)
   * @param amount   the amount to be transferred from one user to another
   *
   * @return true if the transfer was successful, false otherwise
   */
  default boolean transfer(final @NotNull QUser from, final @NotNull QUser to, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {

    if(!valid()) {
      return false;
    }

    if(this.balance(from, world, currency).compareTo(amount) >= 0) {

      if(this.withdraw(from, world, currency, amount)) {

        if(this.deposit(to, world, currency, amount)) {

          this.deposit(from, world, currency, amount);
          return true; //TODO: This was false before which I believe was a bug, but need to test to confirm.
        }
        return false;
      }
      return false;
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
  boolean withdraw(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount);
}