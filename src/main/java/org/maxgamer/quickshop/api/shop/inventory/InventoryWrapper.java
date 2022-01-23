package org.maxgamer.quickshop.api.shop.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;

import java.util.HashMap;
import java.util.Map;

public interface InventoryWrapper {
    /**
     * Returns the size of the inventory
     *
     * @return The size of the inventory
     */
    int getSize();

    /**
     * Returns the ItemStack found in the slot at the given index
     *
     * @param index The index of the Slot's ItemStack to return
     * @return The ItemStack in the slot
     */
    @Nullable ItemStack getItem(int index);

    /**
     * Stores the ItemStack at the given index of the inventory.
     *
     * @param index The index where to put the ItemStack
     * @param item The ItemStack to set
     * @exception IllegalArgumentException Throws IllegalArgumentException if the ItemStack doesn't get support.
     */
    void setItem(int index, @Nullable ItemStack item) throws IllegalArgumentException;

    default int firstPartial(@NotNull Material material) {
        ItemStack[] inventory = getStorageContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }

    default int firstPartial(ItemStack item) {
        ItemStack[] inventory = getStorageContents();
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < inventory.length; i++) {
            ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item)) {
                return i;
            }
        }
        return -1;
    }


    default int getMaxStackSize() {
        return 64;
    }

    default int countItems(@NotNull ItemStack stack){
        return Util.countItems(this,stack);
    }

    default int countSpace(@NotNull ItemStack stack){
        return Util.countSpace(this,stack);
    }

    default int countItems(@NotNull Shop shop){
        return Util.countItems(this,shop);
    }

    default int countSpace(@NotNull Shop shop){
        return Util.countSpace(this,shop);
    }

    /**
     * Stores the given ItemStacks in the inventory. This will try to fill
     * existing stacks and empty slots as well as it can.
     * <p>
     * The returned Map contains what it couldn't store, where the key is
     * the index of the parameter, and the value is the ItemStack at that
     * index of the varargs parameter. If all items are stored, it will return
     * an empty Map.
     * <p>
     * If you pass in ItemStacks which exceed the maximum stack size for the
     * Material, first they will be added to partial stacks where
     * Material.getMaxStackSize() is not exceeded, up to
     * Material.getMaxStackSize(). When there are no partial stacks left
     * stacks will be split on Inventory.getMaxStackSize() allowing you to
     * exceed the maximum stack size for that material.
     * <p>
     * It is known that in some implementations this method will also set
     * the inputted argument amount to the number of that item not placed in
     * slots.
     *
     * @param items The ItemStacks to add
     * @return Map containing items that didn't fit.
     * @throws IllegalArgumentException if items or any element in it is null or ItemStack doesn't get support.
     */
    default @NotNull Map<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException{
        Map<Integer, ItemStack> leftover = new HashMap<>();

        /* TODO: some optimization
         *  - Create a 'firstPartial' with a 'fromIndex'
         *  - Record the lastPartial per Material
         *  - Cache firstEmpty result
         */

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            while (true) {
                // Do we already have a stack of it?
                int firstPartial = firstPartial(item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > getMaxStackSize()) {
                            item.setAmount(getMaxStackSize());
                            setItem(firstFree, item);
                            item.setAmount(item.getAmount() - getMaxStackSize());
                        } else {
                            // Just store it
                            setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = getItem(firstPartial);
                    int amount = item.getAmount();
                    int partialAmount = partialItem.getAmount();
                    int maxAmount = partialItem.getMaxStackSize();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        // To make sure the packet is sent to the client
                        setItem(firstPartial, partialItem);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    // To make sure the packet is sent to the client
                    setItem(firstPartial, partialItem);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }

    /**
     * Removes the given ItemStacks from the inventory.
     * <p>
     * It will try to remove 'as much as possible' from the types and amounts
     * you give as arguments.
     * <p>
     * The returned Map contains what it couldn't remove, where the key is
     * the index of the parameter, and the value is the ItemStack at that
     * index of the varargs parameter. If all the given ItemStacks are
     * removed, it will return an empty Map.
     * <p>
     * It is known that in some implementations this method will also set the
     * inputted argument amount to the number of that item not removed from
     * slots.
     *
     * @param items The ItemStacks to remove
     * @return A Map containing items that couldn't be removed.
     * @throws IllegalArgumentException if items is null or ItemStack doesn't get support.
     */
    default @NotNull Map<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException{
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            int toDelete = item.getAmount();

            while (true) {
                int first = first(item, false);
                // Drat! we don't have this type in the inventory
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                } else {
                    ItemStack itemStack = getItem(first);
                    int amount = itemStack.getAmount();

                    if (amount <= toDelete) {
                        toDelete -= amount;
                        // clear the slot, all used up
                        clear(first);
                    } else {
                        // split the stack and store
                        itemStack.setAmount(amount - toDelete);
                        setItem(first, itemStack);
                        toDelete = 0;
                    }
                }
                // Bail when done
                if (toDelete <= 0) {
                    break;
                }
            }
        }
        return leftover;
    }

    /**
     * Returns all ItemStacks from the inventory
     *
     * @return An array of ItemStacks from the inventory. Individual items may be null.
     */
    @NotNull ItemStack[] getContents();

    /**
     * Completely replaces the inventory's contents. Removes all existing
     * contents and replaces it with the ItemStacks given in the array.
     *
     * @param items A complete replacement for the contents; the length must
     *     be less than or equal to {@link #getSize()}.
     * @throws IllegalArgumentException If the array has more items than the
     *     inventory or ItemStack doesn't get support.
     */
    void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException;

    /**
     * Return the contents from the section of the inventory where items can
     * reasonably be expected to be stored. In most cases this will represent
     * the entire inventory, but in some cases it may exclude armor or result
     * slots.
     * <br>
     * It is these contents which will be used for add / contains / remove
     * methods which look for a specific stack.
     *
     * @return inventory storage contents. Individual items may be null.
     */
    @NotNull ItemStack[] getStorageContents();

    /**
     * Put the given ItemStacks into the storage slots
     *
     * @param items The ItemStacks to use as storage contents
     * @throws IllegalArgumentException If the array has more items than the
     * inventory or ItemStack doesn't get support.
     */
    void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException;

    /**
     * Checks if the inventory contains any ItemStacks with the given
     * material.
     *
     * @param material The material to check for
     * @return true if an ItemStack is found with the given Material
     */
    default boolean contains(@NotNull Material material){
        for (ItemStack item : getStorageContents()) {
            if (item.getType() == material) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the inventory contains any ItemStacks matching the given
     * ItemStack.
     * <p>
     * This will only return true if both the type and the amount of the stack
     * match.
     *
     * @param item The ItemStack to match against
     * @return false if item is null, true if any exactly matching ItemStacks
     *     were found
     */
    @Contract("null -> false")
    default boolean contains(@Nullable ItemStack item){
        if (item == null) {
            return false;
        }
        for (ItemStack i : getStorageContents()) {
            if (item.equals(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the inventory contains any ItemStacks with the given
     * material, adding to at least the minimum amount specified.
     *
     * @param material The material to check for
     * @param amount The minimum amount
     * @return true if amount is less than 1, true if enough ItemStacks were
     *     found to add to the given amount
     */
    default boolean contains(@NotNull Material material, int amount) {
        if (amount <= 0) {
            return true;
        }
        for (ItemStack item : getStorageContents()) {
            if (item.getType() == material) {
                if ((amount -= item.getAmount()) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the inventory contains at least the minimum amount specified
     * of exactly matching ItemStacks.
     * <p>
     * An ItemStack only counts if both the type and the amount of the stack
     * match.
     *
     * @param item the ItemStack to match against
     * @param amount how many identical stacks to check for
     * @return false if item is null, true if amount less than 1, true if
     *     amount of exactly matching ItemStacks were found
     * @see #containsAtLeast(ItemStack, int)
     */
    @Contract("null, _ -> false")
    default boolean contains(@Nullable ItemStack item, int amount){
        if (item == null) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }
        for (ItemStack i : getStorageContents()) {
            if (item.equals(i) && --amount <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the inventory contains ItemStacks matching the given
     * ItemStack whose amounts sum to at least the minimum amount specified.
     *
     * @param item the ItemStack to match against
     * @param amount the minimum amount
     * @return false if item is null, true if amount less than 1, true if
     *     enough ItemStacks were found to add to the given amount
     */
    @Contract("null, _ -> false")
    default boolean containsAtLeast(@Nullable ItemStack item, int amount){
        if (item == null) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }
        for (ItemStack i : getStorageContents()) {
            if (item.isSimilar(i) && (amount -= i.getAmount()) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a Map with all slots and ItemStacks in the inventory with
     * the given Material.
     * <p>
     * The Map contains entries where, the key is the slot index, and the
     * value is the ItemStack in that slot. If no matching ItemStack with the
     * given Material is found, an empty map is returned.
     *
     * @param material The material to look for
     * @return A Map containing the slot index, ItemStack pairs
     */
    default @NotNull Map<Integer, ? extends ItemStack> all(@NotNull Material material) {
        Map<Integer, ItemStack> slots = new HashMap<>();

        ItemStack[] inventory = getStorageContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material) {
                slots.put(i, item);
            }
        }
        return slots;
    }

    /**
     * Finds all slots in the inventory containing any ItemStacks with the
     * given ItemStack. This will only match slots if both the type and the
     * amount of the stack match
     * <p>
     * The Map contains entries where, the key is the slot index, and the
     * value is the ItemStack in that slot. If no matching ItemStack with the
     * given Material is found, an empty map is returned.
     *
     * @param item The ItemStack to match against
     * @return A map from slot indexes to item at index
     */
    default @NotNull Map<Integer, ? extends ItemStack> all(@Nullable ItemStack item){
        Map<Integer, ItemStack> slots = new HashMap<>();
        if (item != null) {
            ItemStack[] inventory = getStorageContents();
            for (int i = 0; i < inventory.length; i++) {
                if (item.equals(inventory[i])) {
                    slots.put(i, inventory[i]);
                }
            }
        }
        return slots;
    }

    default int first(ItemStack item, boolean withAmount) {
        if (item == null) {
            return -1;
        }
        ItemStack[] inventory = getStorageContents();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) continue;

            if (withAmount ? item.equals(inventory[i]) : item.isSimilar(inventory[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first slot in the inventory containing an ItemStack with the
     * given material
     *
     * @param material The material to look for
     * @return The slot index of the given Material or -1 if not found
     */
   default int first(Material material) {
        ItemStack[] inventory = getStorageContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first slot in the inventory containing an ItemStack with
     * the given stack. This will only match a slot if both the type and the
     * amount of the stack match
     *
     * @param item The ItemStack to match against
     * @return The slot index of the given ItemStack or -1 if not found
     */
    default int first(@NotNull ItemStack item){
        return first(item, true);
    }

    /**
     * Returns the first empty Slot.
     *
     * @return The first empty Slot found, or -1 if no empty slots.
     */
    default int firstEmpty(){
        ItemStack[] inventory = getStorageContents();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check whether or not this inventory is empty. An inventory is considered
     * to be empty if there are no ItemStacks in any slot of this inventory.
     *
     * @return true if empty, false otherwise
     */
    default boolean isEmpty(){
        return getStorageContents().length == 0;
    }


    /**
     * Removes all stacks in the inventory matching the given material.
     *
     * @param material The material to remove
     */
    default void remove(@NotNull Material material) {
        ItemStack[] items = getStorageContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() == material) {
                clear(i);
            }
        }
    }

    /**
     * Removes all stacks in the inventory matching the given stack.
     * <p>
     * This will only match a slot if both the type and the amount of the
     * stack match
     *
     * @param item The ItemStack to match against
     */
    default void remove(@NotNull ItemStack item){
        ItemStack[] items = getStorageContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].equals(item)) {
                clear(i);
            }
        }
    }

    /**
     * Clears out a particular slot in the index.
     *
     * @param index The index to empty.
     */
    default void clear(int index){
        setItem(index, null);
    }

    /**
     * Clears out the whole Inventory.
     */
    default void clear(){
        for (int i = 0; i < getSize(); i++) {
            clear(i);
        }
    }

    /**
     * Gets the block or entity belonging to the open inventory
     *
     * @return The holder of the inventory; null if it has no holder.
     */
    @Nullable InventoryHolder getHolder();

    /**
     * Get the location of the block or entity which corresponds to this inventory. May return null if this container
     * was custom created or is a virtual / subcontainer.
     *
     * @return location or null if not applicable.
     */
    @Nullable Location getLocation();

    /**
     * Gets the Inventory Type
     * @return The Inventory Type
     */
    @NotNull InventoryWrapperType getInventoryType();

    /**
     * Gets the Inventory Wrapper Manager
     * @return Wrapper Manager
     */
    @NotNull InventoryWrapperManager getWrapperManager();

    /**
     * Do valid check, check if this Inventory is valid.
     * @return valid
     */
    default boolean isValid(){
        return true;
    }

}
