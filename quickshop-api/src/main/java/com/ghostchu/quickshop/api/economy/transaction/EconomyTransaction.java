package com.ghostchu.quickshop.api.economy.transaction;
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
import com.ghostchu.quickshop.api.operation.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

/**
 * EconomyTransaction
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface EconomyTransaction {

  /**
   * Retrieves the currency associated with this transaction.
   *
   * @return a String value representing the currency of the transaction, or null if no currency is
   * set
   */
  @Nullable
  String currency();

  /**
   * Sets the currency for the transaction.
   *
   * @param currency the currency to be set for the transaction
   */
  void currency(final @Nullable String currency);

  /**
   * Retrieves the amount associated with this transaction.
   *
   * @return the BigDecimal value representing the amount of the transaction
   */
  @NotNull
  BigDecimal amount();

  /**
   * Sets the amount for the transaction.
   *
   * @param amount the amount to be set for the transaction
   */
  void amount(final @NotNull BigDecimal amount);

  /**
   * Retrieves the world associated with this transaction.
   *
   * @return a String value representing the world of the transaction
   */
  @NotNull
  String world();

  /**
   * Sets the world associated with this transaction.
   *
   * @param world a String value representing the world of the transaction
   */
  void world(final @NotNull String world);

  /**
   * Retrieves the QUser object representing the user associated with this transaction.
   *
   * @return the QUser object representing the user associated with this transaction, or null if no
   * user is set
   */
  @Nullable
  QUser from();

  /**
   * Sets the QUser representing the user from which the transaction originates.
   *
   * @param user the QUser object representing the user initiating the transaction
   */
  void from(final @Nullable QUser user);

  /**
   * Retrieves the QUser object representing the user associated with this transaction.
   *
   * @return the QUser object representing the user associated with this transaction, or null if no
   * user is set
   */
  @Nullable
  QUser to();

  /**
   * Sets the QUser object representing the user to whom the transaction is targeted.
   *
   * @param user the QUser object representing the user receiving the transaction
   */
  void to(final @Nullable QUser user);

  /**
   * Retrieves the QUser object representing the user responsible for applying taxes in this
   * transaction.
   *
   * @return the QUser object representing the taxer, or null if no taxer is set
   */
  @Nullable
  QUser taxer();

  /**
   * Apply taxes related to a specified user in a transaction.
   *
   * @param user the QUser object representing the user to apply taxes to
   */
  void taxer(final @Nullable QUser user);

  /**
   * Calculates and retrieves the tax amount associated with this transaction
   * based on the defined tax rules or system configuration.
   *
   * @return a BigDecimal representing the calculated tax amount for the transaction
   * @since 6.2.0.11
   */
  BigDecimal toTax();

  /**
   * Sets the tax amount for the current transaction related to the specified user or entity.
   *
   * @param tax the tax amount to be set for the transaction, represented as a BigDecimal
   * @since 6.2.0.11
   */
  void toTax(BigDecimal tax);

  /**
   * Retrieves the tax amount associated with the originator of the transaction.
   *
   * @return the tax amount originating from the source of the transaction, represented as a BigDecimal
   * @since 6.2.0.11
   */
  BigDecimal fromTax();

  /**
   * Sets the tax amount for the current transaction originating from a specific source.
   *
   * @param tax the tax amount to be set, represented as a BigDecimal
   * @since 6.2.0.11
   */
  void fromTax(BigDecimal tax);

  /**
   * Indicates whether the transaction is completable.
   *
   * @return true if the transaction can be completed, false otherwise
   */
  boolean completable();

  /**
   * Commits the current transaction, finalizing all operations made.
   *
   * @return true if the commit operation is successful, false otherwise
   */
  boolean commit();

  /**
   * Safely commits the transaction, ensuring that all operations are successfully committed.
   *
   * @return true if the commit operation is successful, false otherwise
   */
  boolean safeCommit();

  /**
   * Commits the current transaction with the provided callback, finalizing all operations made.
   *
   * @param callback the callback to be executed during the commit process
   *
   * @return true if the commit operation is successful, false otherwise
   */
  boolean commit(@NotNull TransactionCallback callback);

  /**
   * Rollbacks the list of operations in a transaction, with an option to continue rolling back even
   * if one operation fails.
   *
   * @param continueOnFail a boolean flag indicating whether to continue rolling back if an
   *                       operation fails
   *
   * @return a List of Operation objects representing the rollback status of each operation
   */
  List<Operation> rollback(final boolean continueOnFail);
}