package com.ghostchu.quickshop.shop.inventory;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BukkitInventoryWrapper implements InventoryWrapper {
    private final Inventory inventory;
    private final InventoryWrapperManager manager;

    public BukkitInventoryWrapper(@NotNull Inventory inventory) {
        this.inventory = inventory;
        this.manager = QuickShop.getInstance().getInventoryWrapperManager();
    }

    @Override
    public @NotNull InventoryWrapperIterator iterator() {
        return InventoryWrapperIterator.ofBukkitInventory(inventory);
    }

    @Override
    public @NotNull Map<Integer, ItemStack> addItem(ItemStack... itemStacks) {
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
        return this.manager;
    }

    @Override
    public InventoryHolder getHolder() {
        return inventory.getHolder();
    }

    @Override
    public void setContents(ItemStack[] itemStacks) {
        inventory.setStorageContents(itemStacks);
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
