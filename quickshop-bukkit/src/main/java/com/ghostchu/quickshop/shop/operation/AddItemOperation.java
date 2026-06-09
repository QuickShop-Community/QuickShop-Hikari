package com.ghostchu.quickshop.shop.operation;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.api.operation.result.ItemAddOperationResult;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Operation to add items
 */
public class AddItemOperation implements Operation {

  private final ItemStack[] items;
  private final InventoryWrapper inv;
  private final int[] itemMaxStackSize;
  private boolean committed;
  private boolean rollback;
  private ItemStack[] snapshot;


  /**
   * Constructor.
   *
   * @param item   item to add
   * @param amount amount to add
   * @param inv    The {@link InventoryWrapper} to add to
   */
  public AddItemOperation(@NotNull final ItemStack item, final int amount, @NotNull final InventoryWrapper inv) {

    final ItemStack clone = item.clone();
    clone.setAmount(amount);
    this.items = new ItemStack[]{ clone };
    this.inv = inv;
    this.itemMaxStackSize = new int[]{ Util.getItemMaxStackSize(item.getType()) };
  }

  public AddItemOperation(@NotNull final ItemStack[] items, @NotNull final InventoryWrapper inv) {

    this.items = items;
    this.inv = inv;
    this.itemMaxStackSize = Util.getItemMaxStackSizes(items);
  }

  @Override
  public ItemAddOperationResult commit() {
    committed = true;
    snapshot = inv.createSnapshot();

    int totalRemaining = 0;

    for (int i = 0; i < items.length; i++) {
      final ItemStack item = items[i];

      if (item == null || item.getAmount() <= 0) {
        continue;
      }

      int remaining = item.getAmount();
      int previousRemaining = -1;

      while (remaining > 0) {
        final int attemptedAmount = Math.min(remaining, itemMaxStackSize[i]);

        final ItemStack stackToAdd = item.clone();
        stackToAdd.setAmount(attemptedAmount);

        Log.debug("Committing add item operation, remaining: " + remaining + ", attemptedAmount: " + attemptedAmount + ", stackToAdd: " + stackToAdd);

        final Map<Integer, ItemStack> leftovers = inv.addItem(stackToAdd);

        final int leftoverAmount = leftovers.values().stream().filter(Objects::nonNull).mapToInt(ItemStack::getAmount).sum();

        final int addedAmount = attemptedAmount - leftoverAmount;

        if (addedAmount <= 0 || remaining == previousRemaining) {

          totalRemaining += remaining;
          return new ItemAddOperationResult(false, totalRemaining);
        }

        remaining -= addedAmount;
        previousRemaining = remaining;
      }
    }

    return new ItemAddOperationResult(true, 0);
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
    return inv.restoreSnapshot(this.snapshot);
  }

}
