package com.ghostchu.quickshop.shop.display;

/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.shop.datatype.ShopProtectionFlag;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and cannot be interacted
 * with.
 */
public abstract class AbstractDisplayItem implements Reloadable {

  protected static final QuickShop PLUGIN = QuickShop.getInstance();
  protected final ItemStack originalItemStack;
  protected final Shop shop;
  @Nullable
  protected ItemStack guardedStack;
  private boolean pendingRemoval;
  private static boolean virtualDisplayDoesntWork = false;
  private static final NamespacedKey DISPLAY_MARK_NAMESPACE = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "display_protection");

  protected AbstractDisplayItem(final Shop shop) {

    this.shop = shop;
    this.originalItemStack = shop.getItem().clone();
    PLUGIN.getReloadManager().register(this);
    init();
  }

  public static void setVirtualDisplayDoesntWork(final boolean shouldDisable) {

    virtualDisplayDoesntWork = shouldDisable;
  }

  /**
   * Get PLUGIN now is using which one DisplayType
   *
   * @return Using displayType.
   */
  @NotNull
  public static DisplayType getNowUsing() {

    final DisplayType displayType = DisplayType.fromID(PLUGIN.getConfig().getInt("shop.display-type"));
    if(displayType == DisplayType.VIRTUALITEM && virtualDisplayDoesntWork) {
      return DisplayType.CUSTOM;
    }
    return displayType;
  }

  /**
   * Check the itemStack is contains protect flag.
   *
   * @param itemStack Target ItemStack
   *
   * @return Contains protect flag.
   */
  public static boolean checkIsGuardItemStack(@Nullable final ItemStack itemStack) {

    if(!PLUGIN.isDisplayEnabled()) {
      return false;
    }
    if(getNowUsing() == DisplayType.VIRTUALITEM) {
      return false;
    }
    Util.ensureThread(false);
    if(itemStack == null) {
      return false;
    }
    if(!itemStack.hasItemMeta()) {
      return false;
    }
    final ItemMeta iMeta = itemStack.getItemMeta();
    if(iMeta == null) {
      return false;
    }
    return iMeta.getPersistentDataContainer().has(DISPLAY_MARK_NAMESPACE);
  }

  protected void init() {

    if(PLUGIN.getConfig().getBoolean("shop.display-allow-stacks")) {
      //Prevent stack over the normal size
      originalItemStack.setAmount(Math.min(originalItemStack.getAmount(), originalItemStack.getMaxStackSize()));
    } else {
      this.originalItemStack.setAmount(1);
    }
  }


  /**
   * Create a new itemStack with protect flag.
   *
   * @param itemStack Old itemStack
   * @param shop      The shop
   *
   * @return New itemStack with protect flag.
   */
  @NotNull
  public static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull final Shop shop) {

    itemStack = itemStack.clone();
    ItemMeta iMeta = itemStack.getItemMeta();
    if(iMeta == null) {
      iMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    }
    if(iMeta == null) {
      Log.debug("ItemStack " + itemStack + " cannot getting or creating ItemMeta, failed to create guarded ItemStack.");
      return itemStack;
    }
    if(!PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
      iMeta.setDisplayName(null);
    }
    final ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack, shop);
    itemStack.getItemMeta().getPersistentDataContainer().set(DISPLAY_MARK_NAMESPACE, PersistentDataType.STRING, JsonUtil.getGson().toJson(shopProtectionFlag));
    itemStack.setItemMeta(iMeta);
    return itemStack;
  }

  /**
   * Create the shop protection flag for display item.
   *
   * @param itemStack The item stack
   * @param shop      The shop
   *
   * @return ShopProtectionFlag obj
   */
  @NotNull
  public static ShopProtectionFlag createShopProtectionFlag(
          @NotNull final ItemStack itemStack, @NotNull final Shop shop) {

    return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
  }

  /**
   * Check the display is or not moved.
   *
   * @return Moved
   */
  public abstract boolean checkDisplayIsMoved();

  /**
   * Check the display is or not need respawn
   *
   * @return Need
   */
  public abstract boolean checkDisplayNeedRegen();

  /**
   * Check target Entity is or not a QuickShop display Entity.
   *
   * @param entity Target entity
   *
   * @return Is or not
   */
  public abstract boolean checkIsShopEntity(Entity entity);

  /**
   * Fix the display moved issue.
   */
  public abstract void fixDisplayMoved();

  /**
   * Fix display need respawn issue.
   */
  public abstract void fixDisplayNeedRegen();

  /**
   * Get the display entity
   *
   * @return Target entity
   */
  public abstract Entity getDisplay();

  /**
   * Gets the display location for an item. If it is a double shop and it is not the left shop, it
   * will average the locations of the two chests comprising it to be perfectly in the middle. If it
   * is the left shop, it will return null since the left shop does not spawn an item. Otherwise, it
   * will give you the middle of the single chest.
   *
   * @return The Location that the item *should* be displaying at.
   */
  public @Nullable Location getDisplayLocation() {

    return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
  }

  /**
   * Gets the original ItemStack (without protection mark, should same with shop trading item.
   *
   * @return ItemStack
   */
  @NotNull
  public ItemStack getOriginalItemStack() {

    return originalItemStack;
  }

  /**
   * Gets this display item should be remove
   *
   * @return the status
   */
  public boolean isPendingRemoval() {

    return pendingRemoval;
  }

  /**
   * Check the display is or not already spawned
   *
   * @return Spawned
   */
  public abstract boolean isSpawned();

  /**
   * Check if the display should be display for the specificed player Only works with
   * VirtualDisplayItem together as for as now.
   *
   * @param player Target player
   *
   * @return Should display
   */
  public abstract boolean isApplicableForPlayer(Player player);

  /**
   * Sets this display item should be remove
   */
  public void pendingRemoval() {

    pendingRemoval = true;
  }

  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  /**
   * Remove the display entity.
   *
   * @param dontTouchWorld When it is true, display impl should avoid touch the world to avoid
   *                       unload-load loop
   */
  public abstract void remove(boolean dontTouchWorld);

  /**
   * Remove this shop's display in the whole world.(Not whole server)
   *
   * @return Success
   */
  public abstract boolean removeDupe();

  /**
   * Respawn the displays, if it not exist, it will spawn new one.
   */
  public abstract void respawn();

  /**
   * Add the protect flags for entity or entity's hand item. Target entity will got protect by
   * QuickShop
   *
   * @param entity Target entity
   */
  public abstract void safeGuard(@NotNull Entity entity);

  /**
   * Spawn new Displays
   */
  public abstract void spawn();

  /**
   * Gets the shop that display holding
   *
   * @return The shop that display holding
   */
  @NotNull
  public Shop getShop() {

    return shop;
  }

  public static boolean isVirtualDisplayDoesntWork() {

    return virtualDisplayDoesntWork;
  }
}
