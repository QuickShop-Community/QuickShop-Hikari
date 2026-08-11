package com.ghostchu.quickshop.api.inventory;

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper to handle inventory/fake inventory/custom inventory etc.
 */
public interface InventoryWrapper extends Iterable<ItemStack> {

  /**
   * Change items in the inventory by index Set the item-stack type to air or amount to zero will
   * remove it
   * <p>
   * It's not thread-safe, please use that in main-thread
   *
   * @see ItemChanger
   */
  default void changeItem(final ItemChanger itemChanger) {

    final InventoryWrapperIterator iterator = iterator();
    int index = 0;
    boolean shouldContinue = true;
    while(shouldContinue && iterator.hasNext()) {
      final ItemStack itemStack = iterator.next();
      if(itemStack == null) {
        continue;
      }

      final ItemStack original = itemStack.clone();
      shouldContinue = itemChanger.changeItem(index, itemStack);
      if(itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
        iterator.setCurrent(null);

      } else if(!original.equals(itemStack)) {
        // only update when changed
        iterator.setCurrent(itemStack);
      }
      index++;
    }
  }

  /**
   * Return the iterator for this inventory It's not thread-safe, please use that in main-thread
   *
   * @return the iterator for this inventory
   */
  @NotNull
  @Override
  InventoryWrapperIterator iterator();

  /**
   * Clear the inventory
   */
  void clear();

  /**
   * Create an Inventory snapshot (including Empty slots). QuickShop-Hikari will shoot a snapshot
   * for inventory used for purchase failure rollback. Note: provide an invalid snapshot will cause
   * rollback break whole Inventory! WARNING: High recommend to override this method, default method
   * doing by badways and low-performance, may mess up Inventory item order.
   *
   * @return The inventory contents for snapshot use.
   */
  @NotNull
  default ItemStack[] createSnapshot() {

    Logger.getLogger("QuickShop-Hikari").log(Level.WARNING, "InventoryWrapper provider " + getWrapperManager().getClass().getName() + " didn't override default InventoryWrapper#createSnapshot method, it may cause un-excepted behavior like item missing, mess order and heavy hit performance! Please report this issue to InventoryWrapper provider plugin author!");
    final List<ItemStack> contents = new ArrayList<>();
    for(final ItemStack stack : this) {
      if(stack == null) {
        continue;
      }
      contents.add(stack.clone());
    }
    return contents.toArray(new ItemStack[0]);
  }

  /**
   * Gets the Inventory Wrapper Manager
   *
   * @return Wrapper Manager
   */
  @NotNull
  InventoryWrapperManager getWrapperManager();

  /**
   * Gets the block or entity belonging to the open inventory
   *
   * @return The holder of the inventory; null if it has no holder.
   */
  @Nullable
  InventoryHolder getHolder();

  /**
   * Gets the Inventory Type
   *
   * @return The Inventory Type
   */
  @NotNull
  InventoryWrapperType getInventoryType();

  /**
   * Get the location of the block or entity which corresponds to this inventory. May return null if
   * this container was custom created or is a virtual / subcontainer.
   *
   * @return location or null if not applicable.
   */
  @Nullable
  Location getLocation();

  /**
   * Do valid check, check if this Inventory is valid.
   *
   * @return valid
   */
  default boolean isValid() {

    return true;
  }

  /**
   * Do update check, check if inventory-wrapper should discard and re-create via locateSymbolLink
   *
   * @return should re-locate inventory
   */
  boolean isNeedUpdate();

  /**
   * Remove specific items from inventory
   *
   * @param itemStacks items to remove
   *
   * @return The map of containing item index and itemStack itself which is not fit
   */
  /*@NotNull
  default Map<Integer, ItemStack> removeItem(final ItemStack... itemStacks) {

    if(itemStacks.length == 0) {
      return Collections.emptyMap();
    }
    final InventoryWrapperIterator iterator = iterator();
    final Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
    RemoveProcess:
    for(int i = 0; i < itemStacks.length; i++) {
      final ItemStack itemStackToRemove = itemStacks[i];
      while(iterator.hasNext()) {
        final ItemStack itemStack = iterator.next();
        // TODO: Need lots of verification, it cause mismatch between items under non-Bukkit item matcher
        if(itemStack != null && QuickShopAPI.getInstance().getItemMatcher().matches(itemStackToRemove, itemStack)) {
          final int couldRemove = itemStack.getAmount();
          final int actuallyRemove = Math.min(itemStackToRemove.getAmount(), couldRemove);
          itemStack.setAmount(itemStack.getAmount() - actuallyRemove);
          final int needsNow = itemStackToRemove.getAmount() - actuallyRemove;
          itemStackToRemove.setAmount(needsNow);
          iterator.setCurrent(itemStack);
          if(needsNow == 0) {
            continue RemoveProcess;
          }
        }
      }
      if(itemStackToRemove.getAmount() != 0) {
        integerItemStackMap.put(i, itemStackToRemove);
      }
    }
    return integerItemStackMap;
  }*/

  /* Need to test the following before implementation, but I think it's a better impl.
  */
  @NotNull
  default ItemRemoveResult removeItem(final ItemStack... itemStacks) {

    final Map<Integer, ItemStack> leftovers = new HashMap<>();
    final Map<Integer, ItemStack> removed = new HashMap<>();

    for (int i = 0; i < itemStacks.length; i++) {

      final ItemStack requested = itemStacks[i];
      int remaining = requested.getAmount();

      final InventoryWrapperIterator iterator = iterator();

      int iteratorSlot = 0;
      while (remaining > 0 && iterator.hasNext()) {
        final ItemStack current = iterator.next();

        if (current == null) {
          iteratorSlot++;
          continue;
        }

        if (!QuickShopAPI.getInstance().getItemMatcher().matches(requested, current)) {
          iteratorSlot++;
          continue;
        }

        final ItemStack currentClone = current.clone();

        final int amount = Math.min(remaining, current.getAmount());

        current.setAmount(current.getAmount() - amount);
        currentClone.setAmount(amount);
        iterator.setCurrent(current);

        remaining -= amount;
        if (amount > 0) {

          removed.merge(iteratorSlot, currentClone, (existing, added) -> existing.add(added.getAmount()));
        }
        iteratorSlot++;
      }

      if (remaining > 0) {

        final ItemStack leftoverStack = requested.clone();
        leftoverStack.setAmount(remaining);
        leftovers.put(i, leftoverStack);
      }
    }

    return new ItemRemoveResult(leftovers, removed);
  }

  /**
   * Rollback Inventory by a snapshot. Snapshot can be created by InventoryWrapper#createSnapshot()
   * WARNING: High recommend to override this method, default method doing by badways and
   * low-performance, may mess up Inventory item order.
   *
   * @param snapshot The inventory content snapshot
   *
   * @return The result of rollback.
   */
  default boolean restoreSnapshot(@NotNull final ItemStack[] snapshot) {

    Logger.getLogger("QuickShop-Hikari").log(Level.WARNING, "InventoryWrapper provider " + getWrapperManager().getClass().getName() + " didn't override default InventoryWrapper#restoreSnapshot method, it may cause un-excepted behavior like item missing, mess order and heavy hit performance! Please report this issue to InventoryWrapper provider plugin author!");
    final InventoryWrapperIterator it = iterator();
    while(it.hasNext()) {
      it.remove();
    }
    final Map<Integer, ItemStack> result = addItem(snapshot);
    return result.isEmpty();
  }

  /**
   * Add specific items from inventory
   *
   * @param itemStacks items to add
   *
   * @return The map of containing item index and itemStack itself which is not fit
   */
  @NotNull
  default Map<Integer, ItemStack> addItem(final ItemStack... itemStacks) {

    if(itemStacks.length == 0) {
      return Collections.emptyMap();
    }
    final InventoryWrapperIterator iterator = iterator();
    final Map<Integer, ItemStack> integerItemStackMap = new HashMap<>();
    AddProcess:
    for(int i = 0; i < itemStacks.length; i++) {
      final ItemStack itemStackToAdd = itemStacks[i];
      while(iterator.hasNext()) {
        final ItemStack itemStack = iterator.next();
        if(itemStack == null) {
          iterator.setCurrent(itemStackToAdd);
          itemStackToAdd.setAmount(0);
          continue AddProcess;
        } else {
          if(itemStack.isSimilar(itemStackToAdd)) {
            final int couldAdd = itemStack.getMaxStackSize() - Math.min(itemStack.getMaxStackSize(), itemStack.getAmount());
            final int actuallyAdd = Math.min(itemStackToAdd.getAmount(), couldAdd);
            itemStack.setAmount(itemStack.getAmount() + actuallyAdd);
            final int needsNow = itemStackToAdd.getAmount() - actuallyAdd;
            itemStackToAdd.setAmount(needsNow);
            iterator.setCurrent(itemStack);
            if(needsNow == 0) {
              continue AddProcess;
            }
          }
        }
      }
      if(itemStackToAdd.getAmount() != 0) {
        integerItemStackMap.put(i, itemStackToAdd);
      }
    }
    return integerItemStackMap;
  }

  /**
   * Set the contents of inventory
   *
   * @param itemStacks the contents you want to set
   */
  void setContents(ItemStack[] itemStacks);

  /**
   * Change the item from Inventory
   */
  interface ItemChanger {

    /**
     * Do item change action in the inventory
     *
     * @param index     the item index in the inventory, start from zero
     * @param itemStack the item in this index
     *
     * @return If continue to change items in the next index
     */
    boolean changeItem(int index, ItemStack itemStack);
  }

}
