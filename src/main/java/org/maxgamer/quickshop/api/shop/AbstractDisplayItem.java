/*
 * This file is a part of project QuickShop, the name is AbstractDisplayItem.java
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

package org.maxgamer.quickshop.api.shop;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.ShopProtectionFlag;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.logging.Level;

/**
 * @author Netherfoam
 * A display item, that spawns a block above the chest and cannot be interacted
 * with.
 */
public abstract class AbstractDisplayItem implements Reloadable {

    protected static final QuickShop PLUGIN = QuickShop.getInstance();

    private static boolean DISPLAY_ALLOW_STACKS;
    @Setter
    @Getter
    private static volatile boolean isNotSupportVirtualItem = false;
    protected final ItemStack originalItemStack;
    protected final Shop shop;
    @Nullable
    protected ItemStack guardedIstack;
    private boolean pendingRemoval;

    protected AbstractDisplayItem(Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
        PLUGIN.getReloadManager().register(this);
        init();
    }

    /**
     * Check the itemStack is contains protect flag.
     *
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    public static boolean checkIsGuardItemStack(@Nullable final ItemStack itemStack) {
        if (!PLUGIN.isDisplayEnabled()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }
        Util.ensureThread(false);
        if (itemStack == null) {
            return false;
        }
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        //noinspection ConstantConditions
        for (String lore : iMeta.getLore()) {
            try {
                if (!MsgUtil.isJson(lore)) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = JsonUtil.getGson().fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (defaultMark.equals(ShopProtectionFlag.getMark())) {
                    return true;
                }
                if (shopProtectionFlag.getShopLocation() != null) {
                    return true;
                }
                if (shopProtectionFlag.getItemStackString() != null) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }

        return false;
    }

    /**
     * Check the itemStack is target shop's display
     *
     * @param itemStack Target ItemStack
     * @param shop      Target shop
     * @return Is target shop's display
     */
    public static boolean checkIsTargetShopDisplay(@NotNull final ItemStack itemStack, @NotNull Shop shop) {
        if (!PLUGIN.isDisplayEnabled()) {
            return false;
        }
        if (getNowUsing() == DisplayType.VIRTUALITEM) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore()) {
            return false;
        }
        String defaultMark = ShopProtectionFlag.getDefaultMark();
        String shopLocation = shop.getLocation().toString();
        String attachedShopLocation = "null";
        if (shop.isRealDouble()) {
            attachedShopLocation = shop.getAttachedShop().getLocation().toString();
        }
        //noinspection ConstantConditions
        for (String lore : iMeta.getLore()) {
            try {
                if (!lore.startsWith("{")) {
                    continue;
                }
                ShopProtectionFlag shopProtectionFlag = JsonUtil.getGson().fromJson(lore, ShopProtectionFlag.class);
                if (shopProtectionFlag == null) {
                    continue;
                }
                if (!ShopProtectionFlag.getMark().equals(defaultMark)) {
                    continue;
                }
                if (shopProtectionFlag.getShopLocation().equals(shopLocation)
                        || shopProtectionFlag.getShopLocation().equals(attachedShopLocation)) {
                    return true;
                }
            } catch (JsonSyntaxException e) {
                // Ignore
            }
        }
        return false;
    }

    /**
     * Get PLUGIN now is using which one DisplayType
     *
     * @return Using displayType.
     */
    @NotNull
    public static DisplayType getNowUsing() {
        DisplayType displayType = DisplayType.fromID(PLUGIN.getConfig().getInt("shop.display-type"));
        //Falling back to RealDisplayItem when VirtualDisplayItem is unsupported
        if (isNotSupportVirtualItem && displayType == DisplayType.VIRTUALITEM) {
            PLUGIN.getConfig().set("shop.display-type", 0);
            PLUGIN.saveConfiguration();
            PLUGIN.getLogger().log(Level.WARNING, "Falling back to RealDisplayItem because " + displayType.name() + " type is unsupported");
            return DisplayType.REALITEM;
        }
        if (displayType == DisplayType.UNKNOWN) {
            if (isNotSupportVirtualItem) {
                PLUGIN.getConfig().set("shop.display-type", 0);
                PLUGIN.saveConfiguration();
                PLUGIN.getLogger().log(Level.WARNING, "Falling back to RealDisplayItem because " + displayType.name() + " type is unsupported");
                return DisplayType.REALITEM;
            } else {
                PLUGIN.getConfig().set("shop.display-type", 2);
                PLUGIN.saveConfiguration();
                PLUGIN.getLogger().log(Level.WARNING, "Falling back to VirtualDisplayItem because " + displayType.name() + " type is unsupported");
                return DisplayType.VIRTUALITEM;
            }
        }
        return displayType;
    }

    /**
     * Create a new itemStack with protect flag.
     *
     * @param itemStack Old itemStack
     * @param shop      The shop
     * @return New itemStack with protect flag.
     */
    @NotNull
    public static ItemStack createGuardItemStack(@NotNull ItemStack itemStack, @NotNull Shop shop) {
        itemStack = itemStack.clone();
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta == null) {
            iMeta = PLUGIN.getServer().getItemFactory().getItemMeta(itemStack.getType());
        }
        if (iMeta == null) {
            Util.debugLog("ItemStack " + itemStack + " cannot getting or creating ItemMeta, failed to create guarded ItemStack.");
            return itemStack;
        }
        if (PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
            if (iMeta.hasDisplayName()) {
                iMeta.setDisplayName(iMeta.getDisplayName());
            } else {
                iMeta.setDisplayName(Util.getItemStackName(itemStack));
            }
        } else {
            iMeta.setDisplayName(null);
        }
        ShopProtectionFlag shopProtectionFlag = createShopProtectionFlag(itemStack, shop);
        String protectFlag = JsonUtil.getGson().toJson(shopProtectionFlag);
        iMeta.setLore(Lists.newArrayList(protectFlag));
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    /**
     * Create the shop protection flag for display item.
     *
     * @param itemStack The item stack
     * @param shop      The shop
     * @return ShopProtectionFlag obj
     */
    @NotNull
    public static ShopProtectionFlag createShopProtectionFlag(
            @NotNull ItemStack itemStack, @NotNull Shop shop) {
        return new ShopProtectionFlag(shop.getLocation().toString(), Util.serialize(itemStack));
    }

    protected void init() {
        DISPLAY_ALLOW_STACKS = PLUGIN.getConfig().getBoolean("shop.display-allow-stacks");
        if (DISPLAY_ALLOW_STACKS) {
            //Prevent stack over the normal size
            originalItemStack.setAmount(Math.min(originalItemStack.getAmount(), originalItemStack.getMaxStackSize()));
        } else {
            this.originalItemStack.setAmount(1);
        }
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
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
     * Remove the display entity.
     */
    public abstract void remove();

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
     * Get the display entity
     *
     * @return Target entity
     */
    public abstract Entity getDisplay();

    /**
     * Gets the display location for an item. If it is a double shop and it is not the left shop,
     * it will average the locations of the two chests comprising it to be perfectly in the middle.
     * If it is the left shop, it will return null since the left shop does not spawn an item.
     * Otherwise, it will give you the middle of the single chest.
     *
     * @return The Location that the item *should* be displaying at.
     */
    public @Nullable Location getDisplayLocation() {
        //TODO: Rewrite centering item feature, currently implement is buggy and mess
        Util.ensureThread(false);
        if (shop.isRealDouble()) {
            if (shop.isLeftShop()) {
                Util.debugLog("Shop is left shop, so location is null.");
                return null;
            }
            double avgX = (shop.getLocation().getX() + shop.getAttachedShop().getLocation().getX()) / 2;
            double avgZ = (shop.getLocation().getZ() + shop.getAttachedShop().getLocation().getZ()) / 2;
            Location newloc = new Location(shop.getLocation().getWorld(), avgX, shop.getLocation().getY(), avgZ, shop.getLocation().getYaw(), shop.getLocation().getPitch());
            return newloc.add(0.5, 1.2, 0.5);
        }
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Check the display is or not already spawned
     *
     * @return Spawned
     */
    public abstract boolean isSpawned();

    /**
     * Sets this display item should be remove
     */
    public void pendingRemoval() {
        pendingRemoval = true;
    }

    /**
     * Gets this display item should be remove
     *
     * @return the status
     */
    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

}
