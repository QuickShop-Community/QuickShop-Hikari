/*
 * This file is a part of project QuickShop, the name is InventoryWrapper.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.api.shop.inventory;

import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface InventoryWrapper extends Iterable<ItemStack> {

    /**
     * Return the iterator for this inventory
     * It's not thread-safe, please use that in main-thread
     *
     * @return the iterator for this inventory
     */
    @NotNull
    @Override
    InventoryWrapperIterator iterator();

    /**
     * Get the location of the block or entity which corresponds to this inventory. May return null if this container
     * was custom created or is a virtual / subcontainer.
     *
     * @return location or null if not applicable.
     */
    @Nullable Location getLocation();

    /**
     * Remove specific items from inventory
     *
     * @param itemStacks items to remove
     * @return The map of containing item index and itemStack itself which is not fit
     */
    @NotNull
    default Map<Integer, ItemStack> removeItem(ItemStack... itemStacks) {
        if (itemStacks.length == 0) {
            return Collections.emptyMap();
        }
        InventoryWrapperIterator iterator = iterator();
        Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
        RemoveProcess:
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStackToRemove = itemStacks[i];
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                if (itemStack != null && itemStack.isSimilar(itemStackToRemove)) {
                    int couldRemove = itemStack.getAmount();
                    int actuallyRemove = Math.min(itemStackToRemove.getAmount(), couldRemove);
                    itemStack.setAmount(itemStack.getAmount() - actuallyRemove);
                    int needsNow = itemStackToRemove.getAmount() - actuallyRemove;
                    itemStackToRemove.setAmount(needsNow);
                    if (needsNow == 0) {
                        continue RemoveProcess;
                    }
                }
            }
            if (itemStackToRemove.getAmount() != 0) {
                integerItemStackMap.put(i, itemStackToRemove);
            }
        }
        return integerItemStackMap;
    }

    /**
     * Add specific items from inventory
     *
     * @param itemStacks items to add
     * @return The map of containing item index and itemStack itself which is not fit
     */
    @NotNull
    default Map<Integer, ItemStack> addItem(ItemStack... itemStacks) {
        if (itemStacks.length == 0) {
            return Collections.emptyMap();
        }
        InventoryWrapperIterator iterator = iterator();
        Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
        AddProcess:
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStackToAdd = itemStacks[i];
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                if (itemStack == null) {
                    iterator.setCurrent(itemStackToAdd);
                    itemStackToAdd.setAmount(0);
                    continue AddProcess;
                } else {
                    if (itemStack.isSimilar(itemStackToAdd)) {
                        int couldAdd = itemStack.getMaxStackSize() - Math.min(itemStack.getMaxStackSize(), itemStack.getAmount());
                        int actuallyAdd = Math.min(itemStackToAdd.getAmount(), couldAdd);
                        itemStack.setAmount(itemStack.getAmount() + actuallyAdd);
                        int needsNow = itemStackToAdd.getAmount() - actuallyAdd;
                        itemStackToAdd.setAmount(needsNow);
                        if (needsNow == 0) {
                            continue AddProcess;
                        }
                    }
                }
            }
            if (itemStackToAdd.getAmount() != 0) {
                integerItemStackMap.put(i, itemStackToAdd);
            }
        }
        return integerItemStackMap;
    }

    /**
     * Gets the Inventory Type
     *
     * @return The Inventory Type
     */
    @NotNull InventoryWrapperType getInventoryType();

    /**
     * Gets the Inventory Wrapper Manager
     *
     * @return Wrapper Manager
     */
    @NotNull InventoryWrapperManager getWrapperManager();

    /**
     * Clear the inventory
     */
    void clear();

    /**
     * Gets the block or entity belonging to the open inventory
     *
     * @return The holder of the inventory; null if it has no holder.
     */
    @Nullable InventoryHolder getHolder();

    /**
     * Do valid check, check if this Inventory is valid.
     *
     * @return valid
     */
    default boolean isValid() {
        return true;
    }

}
