package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

public interface InventoryTransaction {
    @Nullable
    InventoryWrapper getFrom();

    void setFrom(@NotNull InventoryWrapper from);

    @Nullable
    InventoryWrapper getTo();

    void setTo(@Nullable InventoryWrapper to);

    @NotNull
    ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    @Nullable
    String getLastError();

    void setLastError(@Nullable String lastError);

    int getAmount();

    void setAmount(int amount);

    @NotNull
    Stack<Operation> getProcessingStack();

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    boolean failSafeCommit();

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
    boolean commit(@NotNull InventoryTransaction.TransactionCallback callback);

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull List<Operation> rollback(boolean continueWhenFailed);

    interface TransactionCallback {
        /**
         * Calling while Transaction commit
         *
         * @param transaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull InventoryTransaction transaction) {
            return true;
        }

        /**
         * Calling while Transaction commit successfully
         *
         * @param transaction Transaction
         */
        default void onSuccess(@NotNull InventoryTransaction transaction) {
        }

        /**
         * Calling while Transaction commit failed
         * Use InventoryTransaction#getLastError() to getting reason
         * Use InventoryTransaction#getSteps() to getting the fail step
         *
         * @param transaction Transaction
         */
        default void onFailed(@NotNull InventoryTransaction transaction) {
        }


    }
}
