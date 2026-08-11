package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.inventory.InventoryTransactionEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.ItemRemoveResult;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.operation.OperationResult;
import com.ghostchu.quickshop.api.operation.result.GenericOperationResult;
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
  private final String itemSerializeString;
  private int amount;
  private String lastError;

  @Builder
  public SimpleInventoryTransaction(@Nullable final InventoryWrapper from, @Nullable final InventoryWrapper to, @NotNull final ItemStack item, final int amount) {

    if(from == null && to == null) {
      throw new IllegalArgumentException("Both from and to are null");
    }
    this.from = from;
    this.to = to;
    this.item = item.clone();
    this.itemSerializeString = Util.serialize(item);
    this.amount = amount;
    new InventoryTransactionEvent(this).callEvent();
  }

  /**
   * Commit the transaction
   *
   * @return The transaction success.
   */
  @Override
  public boolean commit() {

    try(final PerfMonitor ignored = new PerfMonitor("Inventory Transaction - Commit")) {
      return this.commit(new SimpleTransactionCallback() {
      });
    }
  }

  /**
   * Commit the transaction with callback
   *
   * @param callback The result callback
   *
   * @return The transaction success.
   */
  @Override
  public boolean commit(@NotNull final TransactionCallback callback) {

    Log.transaction("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + itemSerializeString);
    if(!callback.onCommit(this)) {
      this.lastError = "Plugin cancelled this transaction.";
      return false;
    }

    OperationResult<?> removeResult = null;
    if(from != null) {

      removeResult = this.executeOperation(new RemoveItemOperation(item, amount, from));
      if(!removeResult.success()) {

        this.lastError = "Failed to remove " + amount + "x " + itemSerializeString + " from " + from;
        callback.onFailed(this);
        return false;
      }
    }

    if(to == null) {

      callback.onSuccess(this);
      return true;
    }

    //TODO: How to make this anti-abusable? Disable it for custom matcher? We can't really guarantee trades for that
    final AddItemOperation addOperation = (removeResult != null && removeResult.result() instanceof ItemRemoveResult)?
                                          new AddItemOperation(((ItemRemoveResult)removeResult.result()).removed().values().toArray(ItemStack[]::new), to) : new AddItemOperation(item, amount, to);
    final OperationResult<?> addResult = this.executeOperation(new AddItemOperation(item, amount, to));

    if(!addResult.success()) {

      this.lastError = "Failed to add " + amount + "x " + itemSerializeString + " to " + to;
      callback.onFailed(this);
      return false;
    }
    callback.onSuccess(this);
    return true;
  }

  @Override
  @Nullable
  public InventoryWrapper getTo() {

    return to;
  }

  @Override
  public void setTo(@Nullable final InventoryWrapper to) {

    this.to = to;
  }

  @Override
  @NotNull
  public ItemStack getItem() {

    return item;
  }

  @Override
  public void setItem(@NotNull final ItemStack item) {

    this.item = item;
  }

  @Override
  @Nullable
  public String getLastError() {

    return lastError;
  }

  @Override
  public void setLastError(@Nullable final String lastError) {

    this.lastError = lastError;
  }

  @Override
  public int getAmount() {

    return amount;
  }

  @Override
  public void setAmount(final int amount) {

    this.amount = amount;
  }

  @Override
  @NotNull
  public Deque<Operation> getProcessingStack() {

    return processingStack;
  }

  /**
   * Commit the transaction by the Fail-Safe way Automatic rollback when commit failed
   *
   * @return The transaction success.
   */
  @Override
  public boolean failSafeCommit() {

    Log.transaction("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + " Item: " + itemSerializeString);
    final boolean result = commit();
    if(!result) {
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

  @Override
  public void setFrom(@Nullable final InventoryWrapper from) {

    this.from = from;
  }

  /**
   * Rolling back the transaction
   *
   * @param continueWhenFailed Continue when some parts of the rollback fails.
   *
   * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains
   * all success steps before hit the error. Else all.
   */
  @SuppressWarnings("UnusedReturnValue")
  @NotNull
  @Override
  public List<Operation> rollback(final boolean continueWhenFailed) {

    try(final PerfMonitor ignored = new PerfMonitor("Inventory Transaction - Rollback")) {
      final List<Operation> operations = new ArrayList<>();
      while(!processingStack.isEmpty()) {
        final Operation operation = processingStack.pop();
        try {
          final boolean result = operation.rollback();
          if(!result) {
            Log.transaction(Level.WARNING, "Rollback failed: " + operation);
            if(continueWhenFailed) {
              operations.add(operation);
              continue;
            } else {
              break;
            }
          } else {
            Log.transaction("Rollback successes: " + operation);
          }
          operations.add(operation);
        } catch(final Exception exception) {
          if(continueWhenFailed) {
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

  private OperationResult<?> executeOperation(@NotNull final Operation operation) {

    try {
      processingStack.push(operation); // Item is special, economy fail won't do anything but item does.
      return operation.commit();
    } catch(final Exception exception) {
      plugin.logger().warn("Failed to execute operation: " + operation, exception);
      this.lastError = "Failed to execute operation: " + operation;
      return new GenericOperationResult(false);
    }
  }

  public interface SimpleTransactionCallback extends InventoryTransaction.TransactionCallback {

    /**
     * Calling while Transaction commit
     *
     * @param transaction Transaction
     *
     * @return Does commit event has been cancelled
     */
    default boolean onCommit(@NotNull final SimpleInventoryTransaction transaction) {

      return true;
    }

    /**
     * Calling while Transaction commit failed Use InventoryTransaction#getLastError() to getting
     * reason Use InventoryTransaction#getSteps() to getting the fail step
     *
     * @param transaction Transaction
     */
    default void onFailed(@NotNull final SimpleInventoryTransaction transaction) {

    }

    /**
     * Calling while Transaction commit successfully
     *
     * @param transaction Transaction
     */
    default void onSuccess(@NotNull final SimpleInventoryTransaction transaction) {

    }

  }

}
