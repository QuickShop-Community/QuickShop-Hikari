/*
 * This file is a part of project QuickShop, the name is EconomyTransaction.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.api.economy;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.event.EconomyCommitEvent;
import org.maxgamer.quickshop.economy.Trader;
import org.maxgamer.quickshop.util.CalculateUtil;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.EconomyTransactionLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class EconomyTransaction {
    @Nullable
    private final UUID from;
    @Nullable
    private final UUID to;
    private final double amount;
    @NotNull
    @JsonUtil.Hidden
    private final EconomyCore core;
    private final double actualAmount;
    private final double tax;
    @Nullable
    private final Trader taxer;
    private final boolean allowLoan;
    private final boolean tryingFixBalanceInsufficient;
    @Getter
    private final World world;
    @Getter
    @Nullable
    private final String currency;
    @JsonUtil.Hidden
    private final QuickShop plugin = QuickShop.getInstance();
    @Getter
    private TransactionSteps steps; //For rollback
    @Nullable
    @Getter
    private String lastError = null;


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
    public EconomyTransaction(@Nullable UUID from, @Nullable UUID to, double amount, double taxModifier, @Nullable Trader taxAccount, EconomyCore core, boolean allowLoan, @NotNull World world, @Nullable String currency) {
        this.from = from;
        this.to = to;
        this.core = core == null ? QuickShop.getInstance().getEconomy() : core;
        this.amount = amount;
        this.steps = TransactionSteps.WAIT;
        this.taxer = taxAccount;
        this.allowLoan = allowLoan;
        this.world = world;
        this.currency = currency;

        if (Double.doubleToLongBits(taxModifier) != Double.doubleToLongBits(0.0d)) { //Calc total money and apply tax
            this.actualAmount = CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), amount);
        } else {
            this.actualAmount = amount;
        }
        this.tax = CalculateUtil.subtract(amount, actualAmount); //Calc total tax
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
        Util.debugLog("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + core.getName());
        boolean result = commit();
        if (!result) {
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
        Util.debugLog("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Total(include tax): " + actualAmount + " Tax: " + tax + ", EconomyCore: " + core.getName());
        steps = TransactionSteps.CHECK;
        if (!callback.onCommit(this)) {
            this.lastError = "Plugin cancelled this transaction.";
            return false;
        }

        if (from != null && core.getBalance(from, world, currency) < amount && !allowLoan) {
            this.lastError = "From hadn't enough money";
            callback.onFailed(this);
            return false;
        }
        steps = TransactionSteps.WITHDRAW;
        if (from != null && !core.withdraw(from, amount, world, currency)) {
            this.lastError = "Failed to withdraw " + amount + " from player " + from + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        steps = TransactionSteps.DEPOSIT;
        if (to != null && !core.deposit(to, actualAmount, world, currency)) {
            this.lastError = "Failed to deposit " + actualAmount + " to player " + to + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        steps = TransactionSteps.TAX;
        if (tax > 0 && taxer != null && !core.deposit(taxer, tax, world, currency)) {
            this.lastError = "Failed to deposit tax account: " + tax + ". LastError: " + core.getLastError();
            callback.onTaxFailed(this);
            //Tax never should failed.
        }
        steps = TransactionSteps.DONE;
        callback.onSuccess(this);
        return true;
    }

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    public List<RollbackSteps> rollback(boolean continueWhenFailed) {
        List<RollbackSteps> rollbackSteps = new ArrayList<>(3);
        if (steps == TransactionSteps.CHECK) {
            return rollbackSteps; //We did nothing, just checks balance
        }
        if (steps == TransactionSteps.WITHDRAW) {
            return rollbackSteps; //We did nothing, because the trade failed so no anybody money changes.
        }
        if (steps == TransactionSteps.DEPOSIT || steps == TransactionSteps.TAX) {
            if (from != null && !core.deposit(from, amount, world, currency)) { //Rollback withdraw
                if (!continueWhenFailed) {
                    rollbackSteps.add(RollbackSteps.ROLLBACK_WITHDRAW);
                    return rollbackSteps;
                }
            }
        }
        if (steps == TransactionSteps.TAX) {
            if (to != null && !core.withdraw(to, actualAmount, world, currency)) { //Rollback deposit
                if (!continueWhenFailed) {
                    rollbackSteps.add(RollbackSteps.ROLLBACK_DEPOSIT);
                    return rollbackSteps;
                }
            }
        }

        rollbackSteps.add(RollbackSteps.ROLLBACK_DONE);
        return rollbackSteps;
    }

    private enum RollbackSteps {
        ROLLBACK_WITHDRAW,
        ROLLBACK_DEPOSIT,
        ROLLBACK_TAX,
        ROLLBACK_DONE
    }

    public enum TransactionSteps {
        WAIT,
        CHECK,
        WITHDRAW,
        DEPOSIT,
        TAX,
        DONE
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
            Util.debugLog("Transaction succeed.");
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(true, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer().getUniqueId(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Transaction commit failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onFailed(@NotNull EconomyTransaction economyTransaction) {
            Util.debugLog("Transaction failed: " + economyTransaction.getLastError() + ".");
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer().getUniqueId(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Tax processing failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onTaxFailed(@NotNull EconomyTransaction economyTransaction) {
            Util.debugLog("Tax Transaction failed: " + economyTransaction.getLastError() + ".");
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer().getUniqueId(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

    }

}
