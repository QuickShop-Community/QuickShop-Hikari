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
    private int remains = 0;
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

    /**
     * Gets the remains items hadn't added into the inventory.
     *
     * @return remains items
     */
    public int getRemains() {
        return remains;
    }

    /**
     * Gets the remains items hadn't rolled back.
     *
     * @return remains items
     */
    public int getRollbackRemains() {
        return rollbackRemains;
    }

    @Override
    public boolean commit() {
        committed = true;
        remains = this.amount;
        while (remains > 0) {
            int stackSize = Math.min(remains, itemMaxStackSize);
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notSaved = inv.addItem(item);
            if (notSaved.isEmpty()) {
                remains -= stackSize;
            } else {
                rollbackRemains -= stackSize - notSaved.entrySet().iterator().next().getValue().getAmount();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean rollback() {
        rollback = true;
        rollbackRemains = remains;
        while (rollbackRemains > 0) {
            int stackSize = Math.min(rollbackRemains, itemMaxStackSize);
            item.setAmount(stackSize);
            Map<Integer, ItemStack> notFit = inv.removeItem(item.clone());
            if (notFit.isEmpty()) {
                rollbackRemains -= stackSize;
            } else {
                remains -= stackSize - notFit.entrySet().iterator().next().getValue().getAmount();
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
}
