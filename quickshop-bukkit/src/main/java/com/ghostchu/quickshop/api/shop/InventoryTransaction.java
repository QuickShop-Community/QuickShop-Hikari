/*
 *  This file is a part of project QuickShop, the name is InventoryTransaction.java
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

package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.InventoryCommitEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.shop.operation.AddItemOperation;
import com.ghostchu.quickshop.api.shop.operation.RemoveItemOperation;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

public class InventoryTransaction {
    @Getter
    private final InventoryWrapper from;
    @Getter
    private final InventoryWrapper to;
    @Getter
    private final ItemStack item;
    @Getter
    private final int amount;
    private final Stack<Operation> processingStack = new Stack<>();
    private final QuickShop plugin = QuickShop.getInstance();
    @Getter
    private String lastError;

    @Builder
    public InventoryTransaction(@Nullable InventoryWrapper from, @Nullable InventoryWrapper to, @NotNull ItemStack item, int amount) {
        if (from == null && to == null)
            throw new IllegalArgumentException("Both from and to are null");
        this.from = from;
        this.to = to;
        this.item = item.clone();
        this.amount = amount;
    }


    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    public boolean failSafeCommit() {
        Util.debugLog("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + Util.serialize(item));
        boolean result = commit();
        if (!result) {
            Util.debugLog("Fail-safe commit failed, starting rollback: " + lastError);
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
        return this.commit(new InventoryTransaction.TransactionCallback() {
            @Override
            public void onSuccess(@NotNull InventoryTransaction inventoryTransaction) {
            }
        });
    }

    /**
     * Commit the transaction with callback
     *
     * @param callback The result callback
     * @return The transaction success.
     */
    public boolean commit(@NotNull InventoryTransaction.TransactionCallback callback) {
        Util.debugLog("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + Util.serialize(item));
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

    private boolean executeOperation(@NotNull Operation operation) {
        if (operation.isCommitted()) {
            throw new IllegalStateException("Operation already committed");
        }
        if (operation.isRollback()) {
            throw new IllegalStateException("Operation already rolled back, you must create another new operation.");
        }
        try {
            if(operation.commit()){
                processingStack.push(operation); // Item is special, economy fail won't do anything but item does.
                return true;
            }
            return false;
        } catch (Exception exception) {
            this.lastError = "Failed to execute operation: " + operation;
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
                Util.debugLog("Operation: " + operation + " is not committed, skip rollback.");
                continue;
            }
            if (operation.isRollback()) {
                Util.debugLog("Operation: " + operation + " is already rolled back, skip rollback.");
                continue;
            }
            try {
                boolean result = operation.rollback();
                if (!result) {
                    Util.debugLog("Rollback failed: " + operation);
                    if (continueWhenFailed) {
                        operations.add(operation);
                        continue;
                    } else {
                        break;
                    }
                }
                Util.debugLog("Rollback successed: " + operation);
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

    public interface TransactionCallback {
        /**
         * Calling while Transaction commit
         *
         * @param transaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull InventoryTransaction transaction) {
            return !Util.fireCancellableEvent(new InventoryCommitEvent(transaction));
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
