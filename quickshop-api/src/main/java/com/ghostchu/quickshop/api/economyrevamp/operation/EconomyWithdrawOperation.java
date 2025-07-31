package com.ghostchu.quickshop.api.economyrevamp.operation;
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

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.economyrevamp.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.operation.Operation;

import java.math.BigDecimal;

/**
 * EconomyDepositOperation
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class EconomyWithdrawOperation implements Operation {

  private final QUser account;
  private final BigDecimal amount;
  private final String world;
  private final String currency;
  private boolean commit = false;
  private boolean rollback = false;

  /**
   * Constructor for creating an EconomyWithdrawOperation object.
   *
   * @param account  the QUser account from which the amount will be withdrawn
   * @param amount   the BigDecimal amount to be withdrawn
   * @param world    the name of the world in which the transaction is performed
   * @param currency the currency type for the transaction
   */
  public EconomyWithdrawOperation(final QUser account, final BigDecimal amount, final String world, final String currency) {

    this.account = account;
    this.amount = amount;
    this.world = world;
    this.currency = currency;
  }

  /**
   * Commit the operation
   *
   * @return true if successes
   */
  @Override
  public boolean commit() {

    final EconomyProvider provider = QuickShopAPI.getInstance().getEconomyManager().provider();
    if(provider == null) {
      return false;
    }

    final boolean result = provider.withdraw(account, world, currency, amount);
    if(result) {
      this.commit = true;
    }
    return result;
  }

  /**
   * Check if operation is committed
   *
   * @return true if committed
   */
  @Override
  public boolean isCommitted() {

    return this.commit;
  }

  /**
   * Check if operation is rolled back
   *
   * @return true if rolled back
   */
  @Override
  public boolean isRollback() {

    return this.rollback;
  }

  /**
   * Rollback the operation
   *
   * @return true if successes
   */
  @Override
  public boolean rollback() {

    final EconomyProvider provider = QuickShopAPI.getInstance().getEconomyManager().provider();
    if(provider == null) {
      return false;
    }

    final boolean result = provider.deposit(account, world, currency, amount);
    if(result) {
      rollback = true;
    }
    return result;
  }
}