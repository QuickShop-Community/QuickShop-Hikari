package com.ghostchu.quickshop.economy.transaction;
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
import com.ghostchu.quickshop.api.economy.benefit.BenefitProvider;
import com.ghostchu.quickshop.api.economy.operation.EconomyDepositOperation;
import com.ghostchu.quickshop.api.economy.operation.EconomyWithdrawOperation;
import com.ghostchu.quickshop.api.economy.transaction.EconomyTransaction;
import com.ghostchu.quickshop.api.economy.transaction.TransactionCallback;
import com.ghostchu.quickshop.api.event.economy.EconomyTransactionEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.common.util.CalculateUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.economy.QSBenefitProvider;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * QSEconomyTransaction
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QSEconomyTransaction implements EconomyTransaction {

  private final Deque<Operation> processingStack = new LinkedList<>();

  @JsonUtil.Hidden
  private final EconomyProvider provider;

  private BenefitProvider benefitProvider;

  private @NotNull String world;
  private @Nullable String currency;
  private @NotNull BigDecimal amount;
  private @NotNull BigDecimal tax = BigDecimal.ZERO;
  private @NotNull BigDecimal amountAfterTax = BigDecimal.ZERO;

  private @Nullable QUser from;
  private @Nullable QUser to;
  private @Nullable QUser taxer;

  private String lastError = "No transaction error logged";

  public QSEconomyTransaction(final BenefitProvider benefitManager, @NotNull final String world,
                              @Nullable final String currency, @NotNull final BigDecimal amount,
                              @NotNull final BigDecimal tax,
                              @Nullable final QUser from, @Nullable final QUser to, @Nullable final QUser taxer) {

    this.benefitProvider = benefitManager;
    this.world = world;
    this.currency = currency;
    this.amount = amount;
    this.from = from;
    this.to = to;
    this.taxer = taxer;

    //Calc total money and apply tax
    if(tax.compareTo(BigDecimal.ZERO) != 0) {
      this.amountAfterTax = CalculateUtil.multiply(CalculateUtil.subtract(BigDecimal.ONE, tax), amount);
    } else {
      this.amountAfterTax = amount;
    }

    this.tax = CalculateUtil.subtract(amount, amountAfterTax); //Calc total tax

    if(from == null && to == null) {
      lastError = "From and To cannot be null in same time.";
      throw new IllegalArgumentException("From and To cannot be null in same time.");
    }

    this.provider = QuickShop.getInstance().getEconomyManager().provider();

    if(this.benefitProvider == null) {
      this.benefitProvider = QSBenefitProvider.EMPTY;
    }

    new EconomyTransactionEvent(this).callEvent();
  }

  public static QSEconomyTransactionBuilder builder() {

    return new QSEconomyTransactionBuilder();
  }

  /**
   * Retrieves the currency associated with this transaction.
   *
   * @return a String value representing the currency of the transaction, or null if no currency is
   * set
   */
  @Override
  public @Nullable String currency() {

    return currency;
  }

  /**
   * Sets the currency for the transaction.
   *
   * @param currency the currency to be set for the transaction
   */
  @Override
  public void currency(final @Nullable String currency) {

    this.currency = currency;
  }

  /**
   * Retrieves the amount associated with this transaction.
   *
   * @return the BigDecimal value representing the amount of the transaction
   */
  @Override
  public @NotNull BigDecimal amount() {

    return amount;
  }

  /**
   * Sets the amount for the transaction.
   *
   * @param amount the amount to be set for the transaction
   */
  @Override
  public void amount(final @NotNull BigDecimal amount) {

    this.amount = amount;
  }

  /**
   * Retrieves the world associated with this transaction.
   *
   * @return a String value representing the world of the transaction
   */
  @Override
  public @NotNull String world() {

    return world;
  }

  /**
   * Sets the world associated with this transaction.
   *
   * @param world a String value representing the world of the transaction
   */
  @Override
  public void world(final @NotNull String world) {

    this.world = world;
  }

  /**
   * Retrieves the QUser object representing the user associated with this transaction.
   *
   * @return the QUser object representing the user associated with this transaction, or null if no
   * user is set
   */
  @Override
  public @Nullable QUser from() {

    return from;
  }

  /**
   * Sets the QUser representing the user from which the transaction originates.
   *
   * @param user the QUser object representing the user initiating the transaction
   */
  @Override
  public void from(final @Nullable QUser user) {

    this.from = user;
  }

  /**
   * Retrieves the QUser object representing the user associated with this transaction.
   *
   * @return the QUser object representing the user associated with this transaction, or null if no
   * user is set
   */
  @Override
  public @Nullable QUser to() {

    return to;
  }

  /**
   * Sets the QUser object representing the user to whom the transaction is targeted.
   *
   * @param user the QUser object representing the user receiving the transaction
   */
  @Override
  public void to(final @Nullable QUser user) {

    this.to = user;
  }

  /**
   * Retrieves the QUser object representing the user responsible for applying taxes in this
   * transaction.
   *
   * @return the QUser object representing the taxer, or null if no taxer is set
   */
  @Override
  public @Nullable QUser taxer() {

    return taxer;
  }

  /**
   * Apply taxes related to a specified user in a transaction.
   *
   * @param user the QUser object representing the user to apply taxes to
   */
  @Override
  public void taxer(final @Nullable QUser user) {

    this.taxer = user;
  }

  /**
   * Retrieves the tax amount associated with this transaction.
   *
   * @return a BigDecimal value representing the tax amount of the transaction
   */
  @Override
  public @NotNull BigDecimal tax() {

    return tax;
  }

  /**
   * Sets the tax for the transaction.
   *
   * @param tax the amount of tax to be set for the transaction
   */
  @Override
  public void tax(final @NotNull BigDecimal tax) {

    this.tax = tax;
  }

  public String lastError() {

    return lastError;
  }

  /**
   * Indicates whether the transaction is completable.
   *
   * @return true if the transaction can be completed, false otherwise
   */
  @Override
  public boolean completable() {

    return from == null || provider.balance(from, world, currency).compareTo(amount) >= 0;
  }

  /**
   * Commits the current transaction, finalizing all operations made.
   *
   * @return true if the commit operation is successful, false otherwise
   */
  @Override
  public boolean commit() {

    try(final PerfMonitor ignored = new PerfMonitor("Economy Transaction - Commit")) {

      return this.commit(new QSTransactionCallback() {
        @Override
        public void onSuccess(@NotNull final QSEconomyTransaction economyTransaction) {

        }
      });
    }
  }

  /**
   * Safely commits the transaction, ensuring that all operations are successfully committed.
   *
   * @return true if the commit operation is successful, false otherwise
   */
  @Override
  public boolean safeCommit() {

    Log.transaction("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + provider.name());
    final boolean result = commit();
    if(!result) {
      Log.transaction(Level.WARNING, "Fail-safe commit failed, starting rollback: " + provider.lastError());
      rollback(true);
    }
    return result;
  }

  /**
   * Commits the current transaction with the provided callback, finalizing all operations made.
   *
   * @param callback the callback to be executed during the commit process
   *
   * @return true if the commit operation is successful, false otherwise
   */
  @Override
  public boolean commit(@NotNull final TransactionCallback callback) {

    Log.transaction("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Total(after tax): " + amountAfterTax + " Tax: " + tax + ", EconomyCore: " + provider.name());
    if(!callback.onCommit(this)) {

      this.lastError = "Plugin cancelled this transaction.";
      return false;
    }

    if(!completable()) {

      this.lastError = "From has insufficient funds.";
      callback.onFailed(this);
      return false;
    }

    if(from != null && !this.executeOperation(new EconomyWithdrawOperation(from, amount, world, currency))) {

      this.lastError = "Failed to withdraw " + amount.toPlainString() + " from account " + from + "LastError: " + provider.lastError();
      return false;
    }

    //if there's no benefits
    if(benefitProvider.none()) {

      if(to != null && !this.executeOperation(new EconomyDepositOperation(to, amountAfterTax, world, currency))) {

        this.lastError = "Failed to deposit " + amountAfterTax.toPlainString() + " to account " + to + "LastError: " + provider.lastError();
        callback.onFailed(this);
        return false;
      }

      callback.onSuccess(this);
      checkTax(callback);
      return true;
    }

    //Benefits exist for this transaction
    final BigDecimal fullAmount = BigDecimal.ONE;
    BigDecimal payout = BigDecimal.ZERO;

    Log.transaction("Benefit processing per-player...");
    for(final Map.Entry<QUser, BigDecimal> entry : benefitProvider.benefits().entrySet()) {

      Log.transaction("Processing benefit for " + entry.getKey() + ", value: " + entry.getValue().toPlainString());
      if(!this.executeOperation(new EconomyDepositOperation(entry.getKey(), amountAfterTax.multiply(entry.getValue()), world, currency))) {

        this.lastError = "Failed to deposit " + amountAfterTax.toPlainString() + " to account " + entry.getKey() + "LastError: " + provider.lastError();
        callback.onFailed(this);
        return false;
      }

      Log.transaction("Benefit for " + entry.getKey() + ", value: " + entry.getValue().toPlainString() + ". Payout = " + payout.toPlainString());
      payout = payout.add(entry.getValue());
    }


    final BigDecimal ownerPayment = CalculateUtil.multiply(amountAfterTax, fullAmount.subtract(payout));
    Log.transaction("Benefit for owner remaining: " + ownerPayment.toPlainString());
    if(ownerPayment.compareTo(BigDecimal.ZERO) > 0 && !this.executeOperation(new EconomyWithdrawOperation(to, ownerPayment, world, currency))) {

      this.lastError = "Failed to deposit " + ownerPayment.toPlainString() + " to account " + to + "LastError: " + provider.lastError();
      callback.onFailed(this);
      return false;
    }

    callback.onSuccess(this);
    checkTax(callback);
    return true;
  }

  private void checkTax(@NotNull final TransactionCallback callback) {

    if(tax.compareTo(BigDecimal.ZERO) > 0) {
      return;
    }

    if(taxer == null) {
      return;
    }

    if(!this.executeOperation(new EconomyDepositOperation(taxer, tax, world, currency))) {

      this.lastError = "Failed to deposit tax to tax account: " + tax.toPlainString() + ". LastError: " + provider.lastError();
      callback.onTaxFailed(this);
    }
  }

  /**
   * Rollbacks the list of operations in a transaction, with an option to continue rolling back even
   * if one operation fails.
   *
   * @param continueOnFail a boolean flag indicating whether to continue rolling back if an
   *                       operation fails
   *
   * @return a List of Operation objects representing the rollback status of each operation
   */
  @Override
  public List<Operation> rollback(final boolean continueOnFail) {

    try(final PerfMonitor ignored = new PerfMonitor("Economy Transaction - Rollback")) {

      final List<Operation> operations = new ArrayList<>();
      while(!processingStack.isEmpty()) {

        final Operation operation = processingStack.pop();
        if(!operation.isCommitted()) {
          continue;
        }

        if(operation.isRollback()) {
          continue;
        }

        try {

          final boolean result = operation.rollback();
          if(!result && !continueOnFail) {
            break;
          }

          operations.add(operation);
        } catch(final Exception ignore) {

          if(!continueOnFail) {

            QuickShop.getInstance().logger().warn("Failed to rollback transaction: {}; Operation: {}; Transaction: {}", provider.lastError(), operation, this);
            break;
          }

          operations.add(operation);
          QuickShop.getInstance().logger().warn("Failed to rollback transaction: {}; Operation: {}; Transaction: {}; Skipping...", provider.lastError(), operation, this);
        }
      }
      return operations;
    }
  }

  private boolean executeOperation(@NotNull final Operation operation) {

    if(operation.isCommitted()) {

      throw new IllegalStateException("Operation already committed");
    }

    if(operation.isRollback()) {

      throw new IllegalStateException("Operation already rolled back, you must create another new operation.");
    }

    try {
      final boolean result = operation.commit();

      if(!result) {

        return false;
      }

      processingStack.push(operation);
      return true;
    } catch(final Exception ignore) {

      QuickShop.getInstance().logger().warn("Failed to execute operation: {}; Operation: {}; Transaction: {}", provider.lastError(), operation, this);
      this.lastError = "Failed to execute operation: " + provider.lastError() + "; Operation: " + operation;
      return false;
    }
  }
}