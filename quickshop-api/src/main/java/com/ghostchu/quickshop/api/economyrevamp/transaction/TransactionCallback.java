package com.ghostchu.quickshop.api.economyrevamp.transaction;
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

import org.jetbrains.annotations.NotNull;

/**
 * TransactionCallback
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface TransactionCallback {

  /**
   * Calling while Transaction commit
   *
   * @param economyTransaction Transaction
   *
   * @return Does commit event has been cancelled
   */
  default boolean onCommit(@NotNull final EconomyTransaction economyTransaction) {

    return true;
  }

  /**
   * Calling while Transaction commit failed Use EconomyTransaction#getLastError() to getting reason
   * Use EconomyTransaction#getSteps() to getting the fail step
   *
   * @param economyTransaction Transaction
   */
  default void onFailed(@NotNull final EconomyTransaction economyTransaction) {

  }

  /**
   * Calling while Transaction commit successfully
   *
   * @param economyTransaction Transaction
   */
  default void onSuccess(@NotNull final EconomyTransaction economyTransaction) {

  }

  /**
   * Calling while Tax processing failed Use EconomyTransaction#getLastError() to getting reason Use
   * EconomyTransaction#getSteps() to getting the fail step
   *
   * @param economyTransaction Transaction
   */
  default void onTaxFailed(@NotNull final EconomyTransaction economyTransaction) {

  }

}