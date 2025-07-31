package com.ghostchu.quickshop.economyrevamp.transaction;
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

import com.ghostchu.quickshop.api.economyrevamp.transaction.TransactionCallback;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * QSTransactionCallback
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface QSTransactionCallback extends TransactionCallback {

  /**
   * Calling while Transaction commit
   *
   * @param economyTransaction Transaction
   *
   * @return Does commit event has been cancelled
   */
  default boolean onCommit(@NotNull final QSEconomyTransaction economyTransaction) {

    return true;
  }

  /**
   * Calling while Transaction commit failed Use EconomyTransaction#getLastError() to getting reason
   * Use EconomyTransaction#getSteps() to getting the fail step
   *
   * @param economyTransaction Transaction
   */
  default void onFailed(@NotNull final QSEconomyTransaction economyTransaction) {

    Log.transaction(Level.WARNING, "Transaction failed: " + economyTransaction.lastError() + ", transaction: " + economyTransaction);
  }

  /**
   * Calling while Transaction commit successfully
   *
   * @param economyTransaction Transaction
   */
  default void onSuccess(@NotNull final QSEconomyTransaction economyTransaction) {

    Log.transaction("Transaction succeed: " + economyTransaction);
  }

  /**
   * Calling while Tax processing failed Use EconomyTransaction#getLastError() to getting reason Use
   * EconomyTransaction#getSteps() to getting the fail step
   *
   * @param economyTransaction Transaction
   */
  default void onTaxFailed(@NotNull final QSEconomyTransaction economyTransaction) {

    Log.transaction(Level.WARNING, "Tax Transaction failed: " + economyTransaction.lastError() + ", transaction: " + economyTransaction);
  }
}