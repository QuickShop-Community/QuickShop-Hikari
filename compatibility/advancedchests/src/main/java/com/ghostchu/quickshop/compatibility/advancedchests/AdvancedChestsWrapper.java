package com.ghostchu.quickshop.compatibility.advancedchests;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.gui.page.ChestPage;

import java.util.Arrays;
import java.util.Map;

public class AdvancedChestsWrapper implements InventoryWrapper {
    private final Main plugin;
    private AdvancedChest<?, ?> advancedChest;

    public AdvancedChestsWrapper(AdvancedChest<?, ?> achest, Main plugin) {
        this.plugin = plugin;
        this.advancedChest = achest;
    }


    /**
     * Return the iterator for this inventory
     * It's not thread-safe, please use that in main-thread
     *
     * @return the iterator for this inventory
     */
    @Override
    public @NotNull InventoryWrapperIterator iterator() {
        ItemStack[] stacks = new ItemStack[0];
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            stacks = ArrayUtils.addAll(stacks, entry.getValue().getItems());
        }
        return InventoryWrapperIterator.ofItemStacks(stacks);
    }

    /**
     * Clear the inventory
     */
    @Override
    public void clear() {
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            ItemStack[] stacks = entry.getValue().getItems();
            Arrays.fill(stacks, null);
            entry.getValue().setPreparedContent(stacks);
        }
    }

    @Override
    public @NotNull ItemStack[] createSnapshot() {
        ItemStack[] stacks = new ItemStack[0];
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            stacks = ArrayUtils.addAll(stacks, entry.getValue().getItems());
        }
        return stacks;
    }

    @NotNull
    private Map<Integer, ChestPage<ItemStack>> getAdvancedChestPages() {
        //noinspection unchecked
        return (Map<Integer, ChestPage<ItemStack>>) advancedChest.getPages();
    }

    /**
     * Gets the Inventory Wrapper Manager
     *
     * @return Wrapper Manager
     */
    @Override
    public @NotNull InventoryWrapperManager getWrapperManager() {
        return plugin.getManager();
    }

    /**
     * Gets the block or entity belonging to the open inventory
     *
     * @return The holder of the inventory; null if it has no holder.
     */
    @Override
    public @Nullable InventoryHolder getHolder() {
        return new AdvancedChestsInventoryHolder(advancedChest);
    }

    /**
     * Gets the Inventory Type
     *
     * @return The Inventory Type
     */
    @Override
    public @NotNull InventoryWrapperType getInventoryType() {
        return InventoryWrapperType.PLUGIN;
    }

    /**
     * Get the location of the block or entity which corresponds to this inventory. May return null if this container
     * was custom created or is a virtual / subcontainer.
     *
     * @return location or null if not applicable.
     */
    @Override
    public @Nullable Location getLocation() {
        return null;
    }

    @Override
    public boolean restoreSnapshot(@NotNull ItemStack[] snapshot) {
        setContents(snapshot);
        return true;
    }

    /**
     * Set the contents of inventory
     *
     * @param itemStacks the contents you want to set
     */
    @Override
    public void setContents(ItemStack[] itemStacks) {
        ItemStack[] contentsPool = itemStacks.clone();
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            ChestPage<ItemStack> page = entry.getValue();
            int slotsPerPage = page.getItems().length;
            ItemStack[] pendingToFill = ArrayUtils.subarray(contentsPool, 0, slotsPerPage - 1);
            contentsPool = ArrayUtils.subarray(contentsPool, slotsPerPage, contentsPool.length - 1);
            page.setPreparedContent(pendingToFill);
        }
    }

    public AdvancedChest<?, ?> getAdvancedChest() {
        return advancedChest;
    }

    static class AdvancedChestsInventoryHolder implements InventoryHolder {
        private final AdvancedChest<?, ?> achest;

        public AdvancedChestsInventoryHolder(AdvancedChest<?, ?> achest) {
            this.achest = achest;
        }

        public AdvancedChest<?, ?> getAdvancedChest() {
            return achest;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
