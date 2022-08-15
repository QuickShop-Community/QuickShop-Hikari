package com.ghostchu.quickshop.api.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Iterator for InventoryWrapper
 */
public interface InventoryWrapperIterator extends Iterator<ItemStack> {


    /**
     * Return the default implementation for bukkit inventory
     *
     * @param inventory bukkit inventory
     * @return the default implementation for bukkit inventory
     */
    static InventoryWrapperIterator ofBukkitInventory(Inventory inventory) {
        int size = inventory.getStorageContents().length;
        return new InventoryWrapperIterator() {
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public ItemStack next() {
                return inventory.getStorageContents()[currentIndex++];
            }

            @Override
            public void setCurrent(ItemStack stack) {
                ItemStack[] storageItems = inventory.getStorageContents();
                storageItems[Math.max(0, currentIndex - 1)] = stack;
                inventory.setStorageContents(storageItems);
            }
        };
    }

    /**
     * Return the default implementation for itemStack array
     *
     * @param itemStacks itemStack array
     * @return the default implementation for itemStack array
     */
    static InventoryWrapperIterator ofItemStacks(ItemStack[] itemStacks) {
        return new InventoryWrapperIterator() {
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < itemStacks.length;
            }

            @Override
            public ItemStack next() {
                return itemStacks[currentIndex++];
            }

            @Override
            public void setCurrent(ItemStack stack) {
                itemStacks[Math.max(0, currentIndex - 1)] = stack;
            }
        };
    }

    @Override
    boolean hasNext();

    /**
     * Get the next ItemStack instance
     * To apply the changes, please use setCurrent method
     *
     * @return the next ItemStack instance
     */
    @Override
    ItemStack next();

    /**
     * Remove the current ItemStack from inventory
     */
    @Override
    default void remove() {
        setCurrent(null);

    }

    /**
     * Set the current ItemStack instance
     *
     * @param stack the itemStack need to set
     */
    void setCurrent(@Nullable ItemStack stack);
}
