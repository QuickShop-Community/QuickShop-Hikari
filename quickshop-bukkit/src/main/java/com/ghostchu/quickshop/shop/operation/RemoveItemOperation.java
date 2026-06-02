package com.ghostchu.quickshop.shop.operation;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.ItemRemoveResult;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.operation.result.ItemRemoveOperationResult;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

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
    int lastRemains = -1;

    final ItemRemoveResult result = new ItemRemoveResult(Collections.emptyMap(), Collections.emptyMap());

    while(remains > 0) {
      final int stackSize = Math.min(remains, itemMaxStackSize);
      item.setAmount(stackSize);
      Log.debug("Committing remove item operation, remains: " + remains + ", stackSize: " + stackSize + ", target: " + item);

      final ItemRemoveResult resultNotFit = inv.removeItem(item.clone());
      result.leftovers().putAll(resultNotFit.leftovers());
      result.removed().putAll(resultNotFit.removed());
      if(resultNotFit.leftovers().isEmpty()) {
        remains -= stackSize;
      } else {
        remains -= stackSize - resultNotFit.leftovers().entrySet().iterator().next().getValue().getAmount();
      }

      if(remains == lastRemains) {
        return new ItemRemoveOperationResult(false, result);
      }
      lastRemains = remains;
    }
    return new ItemRemoveOperationResult(true, result);
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
