package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.InventoryTransactionEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.shop.InventoryTransaction;
import com.ghostchu.quickshop.shop.operation.AddItemOperation;
import com.ghostchu.quickshop.shop.operation.RemoveItemOperation;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import lombok.Builder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class SimpleInventoryTransaction implements InventoryTransaction {
    private final Deque<Operation> processingStack = new LinkedList<>();
    private final QuickShop plugin = QuickShop.getInstance();
    private InventoryWrapper from;
    private InventoryWrapper to;
    private ItemStack item;
    private int amount;
    private String lastError;

    @Builder
    public SimpleInventoryTransaction(@Nullable InventoryWrapper from, @Nullable InventoryWrapper to, @NotNull ItemStack item, int amount) {
        if (from == null && to == null) {
            throw new IllegalArgumentException("Both from and to are null");
        }
        this.from = from;
        this.to = to;
        this.item = item.clone();
        this.amount = amount;
        new InventoryTransactionEvent(this).callEvent();
    }

    public interface SimpleTransactionCallback extends InventoryTransaction.TransactionCallback {
        /**
         * Calling while Transaction commit
         *
         * @param transaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull SimpleInventoryTransaction transaction) {
            return true;
        }

        /**
         * Calling while Transaction commit failed
         * Use InventoryTransaction#getLastError() to getting reason
         * Use InventoryTransaction#getSteps() to getting the fail step
         *
         * @param transaction Transaction
         */
        default void onFailed(@NotNull SimpleInventoryTransaction transaction) {
        }

        /**
         * Calling while Transaction commit successfully
         *
         * @param transaction Transaction
         */
        default void onSuccess(@NotNull SimpleInventoryTransaction transaction) {
        }


    }

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    @Override
    public boolean commit() {
        try (PerfMonitor ignored = new PerfMonitor("Inventory Transaction - Commit")) {
            return this.commit(new SimpleTransactionCallback() {
                @Override
                public void onSuccess(@NotNull SimpleInventoryTransaction inventoryTransaction) {
                }
            });
        }
    }

    /**
     * Commit the transaction with callback
     *
     * @param callback The result callback
     * @return The transaction success.
     */
    @Override
    public boolean commit(@NotNull TransactionCallback callback) {
        Log.transaction("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + Util.serialize(item));
        if (!callback.onCommit(this)) {
            this.lastError = "Plugin cancelled this transaction.";
            return false;
        }
        if (from != null && !this.executeOperation(new RemoveItemOperation(item, amount, from))) {
            this.lastError = "Failed to remove " + amount + "x " + Util.serialize(item) + " from " + from;
            callback.onFailed(this);
            return false;
        }
        if (to != null && !this.executeOperation(new AddItemOperation(item, amount, to))) {
            this.lastError = "Failed to add " + amount + "x " + Util.serialize(item) + " to " + to;
            callback.onFailed(this);
            return false;
        }
        callback.onSuccess(this);
        return true;
    }


    @Override
    public void setFrom(@Nullable InventoryWrapper from) {
        this.from = from;
    }

    @Override
    @Nullable
    public InventoryWrapper getTo() {
        return to;
    }

    @Override
    public void setTo(@Nullable InventoryWrapper to) {
        this.to = to;
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @Override
    public void setItem(@NotNull ItemStack item) {
        this.item = item;
    }

    @Override
    @Nullable
    public String getLastError() {
        return lastError;
    }

    @Override
    public void setLastError(@Nullable String lastError) {
        this.lastError = lastError;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    @NotNull
    public Deque<Operation> getProcessingStack() {
        return processingStack;
    }

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    @Override
    public boolean failSafeCommit() {
        Log.transaction("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + Util.serialize(item));
        boolean result = commit();
        if (!result) {
            Log.transaction(Level.WARNING, "Fail-safe commit failed, starting rollback: " + lastError);
            rollback(true);
        }
        return result;
    }

    @Override
    @Nullable
    public InventoryWrapper getFrom() {
        return from;
    }

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    @Override
    public List<Operation> rollback(boolean continueWhenFailed) {
        try (PerfMonitor ignored = new PerfMonitor("Inventory Transaction - Rollback")) {
            List<Operation> operations = new ArrayList<>();
            while (!processingStack.isEmpty()) {
                Operation operation = processingStack.pop();
                try {
                    boolean result = operation.rollback();
                    if (!result) {
                        Log.transaction(Level.WARNING, "Rollback failed: " + operation);
                        if (continueWhenFailed) {
                            operations.add(operation);
                            continue;
                        } else {
                            break;
                        }
                    } else {
                        Log.transaction("Rollback successes: " + operation);
                    }
                    operations.add(operation);
                } catch (Exception exception) {
                    if (continueWhenFailed) {
                        operations.add(operation);
                        plugin.logger().warn("Failed to rollback transaction: Operation: {}; Transaction: {}; Skipping...", operation, this);
                    } else {
                        plugin.logger().warn("Failed to rollback transaction: Operation: {}; Transaction: {}", operation, this);
                        break;
                    }
                }
            }
            return operations;
        }
    }

    private boolean executeOperation(@NotNull Operation operation) {
        try {
            processingStack.push(operation); // Item is special, economy fail won't do anything but item does.
            return operation.commit();
        } catch (Exception exception) {
            plugin.logger().warn("Failed to execute operation: " + operation, exception);
            this.lastError = "Failed to execute operation: " + operation;
            return false;
        }
    }




}
