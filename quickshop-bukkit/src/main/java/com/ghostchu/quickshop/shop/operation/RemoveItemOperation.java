package com.ghostchu.quickshop.shop.operation;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
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
    private int rollbackRemains = 0;

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
        int remains = amount;
        ItemStack item = this.item.clone();
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            // TODO: BUG! If there no enough item to remove, notFit will be always empty and misleading the rollbackRemains and break rollback logic
            Map<Integer, ItemStack> notFit = inv.removeItem(item.clone());
            if (notFit.isEmpty()) {
                remains -= item.getAmount();
            }else{
                // can't add more items! fast fail!
                rollbackRemains = this.amount - (remains + Util.getItemTotalAmountsInMap(notFit));
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean rollback() {
        rollback = true;
        Log.transaction("DEBUG rollbackRemains "+rollbackRemains);
        int remains  = this.rollbackRemains;
        int lastRemains = -1;
        ItemStack item = this.item.clone();
        while (remains > 0) {
            Log.transaction("DEBUG remains "+remains);
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notSaved = inv.addItem(item);
            if (notSaved.isEmpty()) {
                remains -= item.getAmount();
                Log.transaction("DEBUG notSaved empty"+remains);
            }
            Log.transaction("DEBUG remains now is "+remains);
            if(remains == lastRemains) {
                return false;
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
