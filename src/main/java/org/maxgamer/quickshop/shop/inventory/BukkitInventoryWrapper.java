/*
 * This file is a part of project QuickShop, the name is BukkitInventoryWrapper.java
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

package org.maxgamer.quickshop.shop.inventory;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperIterator;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperType;

import java.util.Map;

public class BukkitInventoryWrapper implements InventoryWrapper {
    private final Inventory inventory;

    public BukkitInventoryWrapper(@NotNull Inventory inventory){
        this.inventory = inventory;
    }

    @Override
    public @NotNull InventoryWrapperIterator iterator() {
        return new BukkitInventoryWrapperIterator(inventory);
    }

    @Override
    public Map<Integer, ItemStack> addItem(ItemStack... itemStacks) {
        return inventory.addItem(itemStacks);
    }

    @Override
    public @Nullable Location getLocation() {
        return inventory.getLocation();
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public @NotNull InventoryWrapperType getInventoryType() {
        return InventoryWrapperType.BUKKIT;
    }

    @Override
    public @NotNull InventoryWrapperManager getWrapperManager() {
        return QuickShop.getInstance().getInventoryWrapperManager();
    }

    @Override
    public boolean hasHolder() {
        return inventory.getHolder() != null;
    }

    static class BukkitInventoryWrapperIterator implements InventoryWrapperIterator {

        int currentIndex = 0;
        Inventory inventory;

        BukkitInventoryWrapperIterator(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public void setCurrent(ItemStack stack) {
            inventory.setItem(currentIndex, stack);
        }

        @Override
        public boolean hasNext() {
            return currentIndex + 1 < inventory.getSize();
        }

        @Override
        public ItemStack next() {
            return inventory.getItem(currentIndex);
        }
    }

    @Override
    public boolean isValid() {
        if (this.inventory instanceof BlockInventoryHolder) {
            if (this.inventory.getLocation() != null) {
                return this.inventory.getLocation().getBlock() instanceof Container;
            }
            return false;
        }
        return true;
    }
}
