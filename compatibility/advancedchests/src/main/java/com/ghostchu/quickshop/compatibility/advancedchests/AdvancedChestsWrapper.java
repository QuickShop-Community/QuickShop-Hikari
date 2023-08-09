package com.ghostchu.quickshop.compatibility.advancedchests;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
import com.ghostchu.quickshop.util.logger.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.gui.page.ChestPage;

import java.util.*;

public class AdvancedChestsWrapper implements InventoryWrapper {
    private final Main plugin;
    private final AdvancedChest<?, ?> advancedChest;

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
            entry.getValue().setContent(Arrays.stream(stacks).toList());
        }
    }

    @Override
    public @NotNull ItemStack[] createSnapshot() {
        ItemStack[] stacks = new ItemStack[0];
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            int id = entry.getKey();
            ChestPage<ItemStack> page = entry.getValue();
            int[] inputSlots = page.getInputSlots();
            ItemStack[] itemsCloneInPage = new ItemStack[inputSlots.length];
            for (int inputSlot : inputSlots) {
                ItemStack stack = page.getBukkitInventory().getItem(inputSlot);
                if (stack != null) {
                    itemsCloneInPage[inputSlot] = stack.clone();
                } else {
                    itemsCloneInPage[inputSlot] = null;
                }
            }
            stacks = ArrayUtils.addAll(stacks, itemsCloneInPage);
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
        Deque<ItemStack> deque = new ArrayDeque<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                deque.add(itemStack);
            }
        }
        for (Map.Entry<Integer, ChestPage<ItemStack>> entry : getAdvancedChestPages().entrySet()) {
            ChestPage<ItemStack> page = entry.getValue();
            int[] inputSlots = page.getInputSlots();
            for (int inputSlot : inputSlots) {
                if (deque.isEmpty()) {
                    break;
                }
                ItemStack stack = deque.pop();
                page.getBukkitInventory().setItem(inputSlot, stack);
            }
        }
        Log.debug("Total " + deque.size() + " items lost due space sufficiency.");
    }

    public AdvancedChest<?, ?> getAdvancedChest() {
        return advancedChest;
    }

    @Override
    public @NotNull Map<Integer, ItemStack> addItem(ItemStack... itemStacks) {
        if (itemStacks.length == 0) {
            return Collections.emptyMap();
        }
        ItemStack[] contents = createSnapshot();
        InventoryWrapperIterator iterator = InventoryWrapperIterator.ofItemStacks(contents);
        Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
        AddProcess:
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStackToAdd = itemStacks[i];
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                if (itemStack == null) {
                    iterator.setCurrent(itemStackToAdd.clone());
                    itemStackToAdd.setAmount(0);
                    continue AddProcess;
                } else {
                    if (itemStack.isSimilar(itemStackToAdd)) {
                        int couldAdd = itemStack.getMaxStackSize() - Math.min(itemStack.getMaxStackSize(), itemStack.getAmount());
                        int actuallyAdd = Math.min(itemStackToAdd.getAmount(), couldAdd);
                        itemStack.setAmount(itemStack.getAmount() + actuallyAdd);
                        int needsNow = itemStackToAdd.getAmount() - actuallyAdd;
                        itemStackToAdd.setAmount(needsNow);
                        iterator.setCurrent(itemStack.clone());
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
        setContents(contents);
        return integerItemStackMap;
    }

    @Override
    public @NotNull Map<Integer, ItemStack> removeItem(ItemStack... itemStacks) {
        if (itemStacks.length == 0) {
            return Collections.emptyMap();
        }
        ItemStack[] contents = createSnapshot();
        InventoryWrapperIterator iterator = InventoryWrapperIterator.ofItemStacks(contents);
        Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
        RemoveProcess:
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStackToRemove = itemStacks[i];
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                // TODO: Need lots of verification, it cause mismatch between items under non-Bukkit item matcher
                if (itemStack != null && QuickShopAPI.getInstance().getItemMatcher().matches(itemStackToRemove, itemStack)) {
                    int couldRemove = itemStack.getAmount();
                    int actuallyRemove = Math.min(itemStackToRemove.getAmount(), couldRemove);
                    itemStack.setAmount(itemStack.getAmount() - actuallyRemove);
                    int needsNow = itemStackToRemove.getAmount() - actuallyRemove;
                    itemStackToRemove.setAmount(needsNow);
                    iterator.setCurrent(itemStack);
                    if (needsNow == 0) {
                        continue RemoveProcess;
                    }
                }
            }
            if (itemStackToRemove.getAmount() != 0) {
                integerItemStackMap.put(i, itemStackToRemove);
            }
        }
        setContents(contents);
        return integerItemStackMap;
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
