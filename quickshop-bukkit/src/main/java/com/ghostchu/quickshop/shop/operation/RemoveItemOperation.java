package com.ghostchu.quickshop.shop.operation;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.ItemRemoveResult;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.operation.result.ItemRemoveOperationResult;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Operation to remove items
 */
public class RemoveItemOperation implements Operation {

  private final ItemStack item;
  private final int amount;
  private final InventoryWrapper inv;
  private final int itemMaxStackSize;
  private boolean committed;
  private boolean rollback;
  private ItemStack[] snapshot;

  /**
   * Constructor
   *
   * @param item   ItemStack to remove
   * @param amount Amount to remove
   * @param inv    The {@link InventoryWrapper} that remove from
   */
  public RemoveItemOperation(@NotNull final ItemStack item, final int amount, @NotNull final InventoryWrapper inv) {

    this.item = item.clone();
    this.amount = amount;
    this.inv = inv;
    this.itemMaxStackSize = Util.getItemMaxStackSize(item.getType());

  }

  @Override
  public ItemRemoveOperationResult commit() {

    committed = true;
    this.snapshot = inv.createSnapshot();
    int remains = amount;

    final List<ItemStack> itemsToRemove = new ArrayList<>();

    while(remains > 0) {
      final int stackSize = Math.min(remains, itemMaxStackSize);
      remains -= stackSize;

      final ItemStack clone = item.clone();
      clone.setAmount(stackSize);

      itemsToRemove.add(clone);
    }

    Log.debug("Committing remove item operation, target: " + itemsToRemove);

    final ItemRemoveResult result = inv.removeItem(itemsToRemove.toArray(new ItemStack[0]));

    Log.debug("Remove item operation results, leftover: " + result.leftovers() + ", removed: " + result.removed());
    return new ItemRemoveOperationResult(result.leftovers().isEmpty(), result);
  }

  @Override
  public boolean isCommitted() {

    return this.committed;
  }

  @Override
  public boolean isRollback() {

    return this.rollback;
  }

  @Override
  public boolean rollback() {

    rollback = true;
    return inv.restoreSnapshot(snapshot);
  }
}
