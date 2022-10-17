package com.ghostchu.quickshop.shop.operation;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
    public RemoveItemOperation(@NotNull ItemStack item, int amount, @NotNull InventoryWrapper inv) {
        this.item = item.clone();
        this.amount = amount;
        this.inv = inv;
        this.itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
    }

    @Override
    public boolean commit() {
        committed = true;
        this.snapshot = inv.createSnapshot();
        int remains = amount;
        ItemStack item = this.item.clone();
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notFit = inv.removeItem(item.clone());
            if (notFit.isEmpty()) {
                remains -= item.getAmount();
            } else {
                // can't add more items! fast fail!
                return false;
            }
        }
        return true;
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
