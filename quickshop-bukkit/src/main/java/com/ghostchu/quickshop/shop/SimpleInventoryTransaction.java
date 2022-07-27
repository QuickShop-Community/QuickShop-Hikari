package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.shop.InventoryTransaction;
import com.ghostchu.quickshop.shop.operation.AddItemOperation;
import com.ghostchu.quickshop.shop.operation.RemoveItemOperation;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Builder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

public class SimpleInventoryTransaction implements InventoryTransaction {
    private final Stack<Operation> processingStack = new Stack<>();
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
    }

    @Override
    @Nullable
    public InventoryWrapper getFrom() {
        return from;
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
    public Stack<Operation> getProcessingStack() {
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

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    @Override
    public boolean commit() {
        return this.commit(new SimpleTransactionCallback() {
            @Override
            public void onSuccess(@NotNull SimpleInventoryTransaction inventoryTransaction) {
            }
        });
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
        List<Operation> operations = new ArrayList<>();
        while (!processingStack.isEmpty()) {
            Operation operation = processingStack.pop();
            if (!operation.isCommitted()) {
                Log.transaction("Operation: " + operation + " is not committed, skip rollback.");
                continue;
            }
            if (operation.isRollback()) {
                Log.transaction("Operation: " + operation + " is already rolled back, skip rollback.");
                continue;
            }
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
                }
                Log.transaction("Rollback successes: " + operation);
                operations.add(operation);
            } catch (Exception exception) {
                if (continueWhenFailed) {
                    operations.add(operation);
                    MsgUtil.debugStackTrace(exception.getStackTrace());
                } else {
                    plugin.getLogger().log(Level.WARNING, "Failed to rollback transaction: Operation: " + operation + "; Transaction: " + this);
                    break;
                }
            }
        }
        return operations;
    }

    private boolean executeOperation(@NotNull Operation operation) {
        if (operation.isCommitted()) {
            throw new IllegalStateException("Operation already committed");
        }
        if (operation.isRollback()) {
            throw new IllegalStateException("Operation already rolled back, you must create another new operation.");
        }
        try {
            if (operation.commit()) {
                processingStack.push(operation); // Item is special, economy fail won't do anything but item does.
                return true;
            }
            return false;
        } catch (Exception exception) {
            this.lastError = "Failed to execute operation: " + operation;
            return false;
        }
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
         * Calling while Transaction commit successfully
         *
         * @param transaction Transaction
         */
        default void onSuccess(@NotNull SimpleInventoryTransaction transaction) {
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


    }


}
