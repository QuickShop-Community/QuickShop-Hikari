/*
 *  This file is a part of project QuickShop, the name is EconomyTransaction.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.api.economy;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.operation.DepositEconomyOperation;
import com.ghostchu.quickshop.api.economy.operation.WithdrawEconomyOperation;
import com.ghostchu.quickshop.api.event.EconomyCommitEvent;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.util.CalculateUtil;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.EconomyTransactionLog;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Transaction
 * A secure way to transfer the money between players.
 * Support rollback :)
 */
@Getter
@ToString
public class EconomyTransaction {
    @Nullable
    private final UUID from;
    @Nullable
    private final UUID to;
    private final double amount;
    @NotNull
    @JsonUtil.Hidden
    private final EconomyCore core;
    private final double amountAfterTax;
    private final double tax;
    @Nullable
    private final UUID taxer;
    private final boolean allowLoan;
    private final boolean tryingFixBalanceInsufficient;
    @Getter
    private final World world;
    @Getter
    @Nullable
    private final String currency;
    @JsonUtil.Hidden
    private final QuickShop plugin = QuickShop.getInstance();
    @Nullable
    @Getter
    private String lastError = null;
    private final Stack<Operation> processingStack = new Stack<>();


    /**
     * Create a transaction
     *
     * @param from        The account that money from, but null will be ignored.
     * @param to          The account that money to, but null will be ignored.
     * @param core        economy core
     * @param allowLoan   allow loan?
     * @param amount      the amount of money
     * @param taxAccount  tax account
     * @param taxModifier tax modifier
     */

    @Builder
    public EconomyTransaction(@Nullable UUID from, @Nullable UUID to, double amount, double taxModifier, @Nullable UUID taxAccount, EconomyCore core, boolean allowLoan, @NotNull World world, @Nullable String currency) {
        this.from = from;
        this.to = to;
        this.core = core == null ? QuickShop.getInstance().getEconomy() : core;
        this.amount = amount;
        this.taxer = taxAccount;
        this.allowLoan = allowLoan;
        this.world = world;
        this.currency = currency;

        if (Double.doubleToLongBits(taxModifier) != Double.doubleToLongBits(0.0d)) { //Calc total money and apply tax
            this.amountAfterTax = CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), amount);
        } else {
            this.amountAfterTax = amount;
        }
        this.tax = CalculateUtil.subtract(amount, amountAfterTax); //Calc total tax
        if (from == null && to == null) {
            lastError = "From and To cannot be null in same time.";
            throw new IllegalArgumentException("From and To cannot be null in same time.");
        }
        //For passing Test
        //noinspection ConstantConditions
        if (QuickShop.getInstance() != null) {
            this.tryingFixBalanceInsufficient = QuickShop.getInstance().getConfig().getBoolean("trying-fix-banlance-insuffient");
        } else {
            this.tryingFixBalanceInsufficient = false;
        }
        if (tryingFixBalanceInsufficient) {
            //Fetch some stupid plugin caching
            if (from != null) {
                this.core.getBalance(from, world, currency);
            }
            if (to != null) {
                this.core.getBalance(to, world, currency);
            }
        }
    }

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    public boolean failSafeCommit() {
        Log.transaction("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + core.getName());
        boolean result = commit();
        if (!result) {
            Log.transaction(Level.WARNING, "Fail-safe commit failed, starting rollback: " + lastError);
            rollback(true);
        }
        return result;
    }

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    public boolean commit() {
        return this.commit(new TransactionCallback() {
            @Override
            public void onSuccess(@NotNull EconomyTransaction economyTransaction) {
                if (tryingFixBalanceInsufficient) {
                    //Fetch some stupid plugin caching
                    if (from != null) {
                        core.getBalance(from, world, currency);
                    }
                    if (to != null) {
                        core.getBalance(to, world, currency);
                    }
                }
            }
        });
    }

    /**
     * Commit the transaction with callback
     *
     * @param callback The result callback
     * @return The transaction success.
     */
    public boolean commit(@NotNull TransactionCallback callback) {
        Log.transaction("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Total(after tax): " + amountAfterTax + " Tax: " + tax + ", EconomyCore: " + core.getName());
        if (!callback.onCommit(this)) {
            this.lastError = "Plugin cancelled this transaction.";
            return false;
        }
        if (!checkBalance()) {
            this.lastError = "From hadn't enough money";
            callback.onFailed(this);
            return false;
        }


        if (from != null && !this.executeOperation(new WithdrawEconomyOperation(from, amount, world, currency, core))) {
            this.lastError = "Failed to withdraw " + amount + " from player " + from + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        if (to != null && !this.executeOperation(new DepositEconomyOperation(to, amountAfterTax, world, currency, core))) {
            this.lastError = "Failed to deposit " + amountAfterTax + " to player " + to + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        if (tax > 0 && taxer != null && !this.executeOperation(new DepositEconomyOperation(taxer, tax, world, currency, core))) {
            this.lastError = "Failed to deposit tax account: " + tax + ". LastError: " + core.getLastError();
            callback.onTaxFailed(this);
            //Tax never should failed.
        }
        callback.onSuccess(this);
        return true;
    }

    /**
     * Checks this transaction can be finished
     *
     * @return The transaction can be finished (had enough money)
     */
    public boolean checkBalance() {
        return from == null || !(core.getBalance(from, world, currency) < amount) || allowLoan;
    }

    /**
     * Getting the tax in this transaction
     *
     * @return The tax in this transaction
     */
    public double getTax() {
        return tax;
    }

    private boolean executeOperation(@NotNull Operation operation) {
        if (operation.isCommitted()) {
            throw new IllegalStateException("Operation already committed");
        }
        if (operation.isRollback()) {
            throw new IllegalStateException("Operation already rolled back, you must create another new operation.");
        }
        try {
            boolean result = operation.commit();
            if (!result)
                return false;
            processingStack.push(operation);
            return true;
        } catch (Exception exception) {
            this.lastError = "Failed to execute operation: " + core.getLastError() + "; Operation: " + operation;
            return false;
        }
    }

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    public List<Operation> rollback(boolean continueWhenFailed) {
        List<Operation> operations = new ArrayList<>();
        while (!processingStack.isEmpty()) {
            Operation operation = processingStack.pop();
            if (!operation.isCommitted()) {
                continue;
            }
            if (operation.isRollback()) {
                continue;
            }
            try {
                boolean result = operation.rollback();
                if (!result) {
                    if (continueWhenFailed) {
                        operations.add(operation);
                        continue;
                    } else {
                        break;
                    }
                }
                operations.add(operation);
            } catch (Exception exception) {
                if (continueWhenFailed) {
                    operations.add(operation);
                    MsgUtil.debugStackTrace(exception.getStackTrace());
                } else {
                    plugin.getLogger().log(Level.WARNING, "Failed to rollback transaction: " + core.getLastError() + "; Operation: " + operation + "; Transaction: " + this);
                    break;
                }
            }
        }
        return operations;
    }


    public interface TransactionCallback {
        /**
         * Calling while Transaction commit
         *
         * @param economyTransaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull EconomyTransaction economyTransaction) {
            return !Util.fireCancellableEvent(new EconomyCommitEvent(economyTransaction));
        }

        /**
         * Calling while Transaction commit successfully
         *
         * @param economyTransaction Transaction
         */
        default void onSuccess(@NotNull EconomyTransaction economyTransaction) {
            Log.transaction("Transaction succeed: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(true, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Transaction commit failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onFailed(@NotNull EconomyTransaction economyTransaction) {
            Log.transaction(Level.WARNING, "Transaction failed: " + economyTransaction.getLastError() + ", transaction: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Tax processing failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onTaxFailed(@NotNull EconomyTransaction economyTransaction) {
            Log.transaction(Level.WARNING, "Tax Transaction failed: " + economyTransaction.getLastError() + ", transaction: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

    }

}
