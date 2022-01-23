package org.maxgamer.quickshop.shop.inventory;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperType;

import java.util.Map;

public class BukkitInventoryWrapper implements InventoryWrapper {
    private final Inventory inventory;

    public BukkitInventoryWrapper(@NotNull Inventory inventory){
        this.inventory = inventory;
    }

    @Override
    public int getSize() {
        return inventory.getSize();
    }

    @Override
    public @Nullable ItemStack getItem(int index) {
        return inventory.getItem(index);
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item) throws IllegalArgumentException {
        inventory.setItem(index,item);
    }

    @Override
    public @NotNull Map<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
       return inventory.removeItem(items);
    }

    @Override
    public @NotNull ItemStack[] getContents() {
        return inventory.getContents();
    }

    @Override
    public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        inventory.setContents(items);
    }

    @Override
    public @NotNull ItemStack[] getStorageContents() {
        return inventory.getStorageContents();
    }

    @Override
    public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        inventory.setStorageContents(items);
    }

    @Override
    public @Nullable InventoryHolder getHolder() {
        return inventory.getHolder();
    }
    @Override
    public @Nullable Location getLocation() {
        return inventory.getLocation();
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
    public boolean isValid() {
        if(this.inventory instanceof BlockInventoryHolder) {
            if(this.inventory.getLocation() != null){
                return this.inventory.getLocation().getBlock() instanceof Container;
            }
            return false;
        }
       return true;
    }
}
