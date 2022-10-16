package com.ghostchu.quickshop.shop.operation;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Operation to add items
 */
public class AddItemOperation implements Operation {
    private final ItemStack item;
    private final int amount;
    private final InventoryWrapper inv;
    private final int itemMaxStackSize;
    private boolean committed;
    private boolean rollback;
    private int rollbackRemains = 0;

    /**
     * Constructor.
     *
     * @param item   item to add
     * @param amount amount to add
     * @param inv    The {@link InventoryWrapper} to add to
     */
    public AddItemOperation(@NotNull ItemStack item, int amount, @NotNull InventoryWrapper inv) {
        this.item = item.clone();
        this.amount = amount;
        this.inv = inv;
        this.itemMaxStackSize = Util.getItemMaxStackSize(item.getType());
    }

    @Override
    public boolean commit() {
        committed = true;
        int remains = this.amount;
        int lastRemains = -1;
        ItemStack item = this.item.clone();
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notSaved = inv.addItem(item);
            if (notSaved.isEmpty()) {
                remains -= stackSize;
            }
            if(remains == lastRemains){
                rollbackRemains = remains;
                return false;
            }
            lastRemains = remains;
        }
        return true;
    }


    @Override
    public boolean rollback() {
        rollback = true;
        int remains = rollbackRemains;
        int lastRemains = -1;
        ItemStack item = this.item.clone();
        while (remains > 0) {
            int stackSize = Math.min(remains, item.getMaxStackSize());
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notFit = inv.removeItem(item);
            if (notFit.isEmpty()) {
                remains -= stackSize;
            }
            if(remains == lastRemains){
                 return true; // Always return true since it is remove!
            }
            lastRemains = remains;
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
}
