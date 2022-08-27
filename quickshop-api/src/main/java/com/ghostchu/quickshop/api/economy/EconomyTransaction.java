package com.ghostchu.quickshop.api.economy;

import com.ghostchu.quickshop.api.operation.Operation;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;
import java.util.UUID;

public interface EconomyTransaction {

    /**
     * Checks this transaction can be finished
     *
     * @return The transaction can be finished (had enough money)
     */
    boolean checkBalance();

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    boolean commit();

    /**
     * Commit the transaction with callback
     *
     * @param callback The result callback
     * @return The transaction success.
     */
    boolean commit(@NotNull TransactionCallback callback);

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    boolean failSafeCommit();

    double getAmount();

    void setAmount(double amount);

    double getAmountAfterTax();

    void setAmountAfterTax(double amountAfterTax);

    @NotNull EconomyCore getCore();

    void setCore(@NotNull EconomyCore core);

    @Nullable String getCurrency();

    void setCurrency(@Nullable String currency);

    @Nullable UUID getFrom();

    void setFrom(@Nullable UUID from);

    @Nullable String getLastError();

    void setLastError(@NotNull String lastError);

    @NotNull Stack<Operation> getProcessingStack();

    /**
     * Getting the tax in this transaction
     *
     * @return The tax in this transaction
     */
    double getTax();

    void setTax(double tax);

    @Nullable UUID getTaxer();

    void setTaxer(@Nullable UUID taxer);

    @Nullable UUID getTo();

    void setTo(@Nullable UUID to);

    @NotNull World getWorld();

    void setWorld(@NotNull World world);

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull List<Operation> rollback(boolean continueWhenFailed);

    void setAllowLoan(boolean allowLoan);

    void setTryingFixBalanceInsufficient(boolean tryingFixBalanceInsufficient);


    interface TransactionCallback {
        /**
         * Calling while Transaction commit
         *
         * @param economyTransaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull EconomyTransaction economyTransaction) {
            return true;
        }

        /**
         * Calling while Transaction commit failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onFailed(@NotNull EconomyTransaction economyTransaction) {
        }

        /**
         * Calling while Transaction commit successfully
         *
         * @param economyTransaction Transaction
         */
        default void onSuccess(@NotNull EconomyTransaction economyTransaction) {

        }

        /**
         * Calling while Tax processing failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onTaxFailed(@NotNull EconomyTransaction economyTransaction) {
        }

    }

}