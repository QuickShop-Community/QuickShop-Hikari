/*
 *  This file is a part of project QuickShop, the name is RealDisplayItem.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.shop.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.ContainerShop;
import io.papermc.lib.PaperLib;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ghostchu.quickshop.api.event.ShopDisplayItemDespawnEvent;
import com.ghostchu.quickshop.api.event.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.api.shop.DisplayType;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ToString
public class RealDisplayItem extends AbstractDisplayItem {

    @Nullable
    private Item item;

    /**
     * ZZ Creates a new display item.
     *
     * @param shop The shop (See Shop)
     */
    public RealDisplayItem(@NotNull Shop shop) {
        super(shop);
    }

    @Override
    public boolean checkDisplayIsMoved() {
        Util.ensureThread(false);
        if (this.item == null) {
            return false;
        }
        if (shop.isLeftShop()) {
            return false;
        }
        /* We give 0.6 block to allow item drop on the chest, not floating on the air. */
        if (!Objects.requireNonNull(this.item.getLocation().getWorld())
                .equals(Objects.requireNonNull(getDisplayLocation()).getWorld())) {
            return true;
        }
        return this.item.getLocation().distance(getDisplayLocation()) > 0.6;
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        Util.ensureThread(false);
        if (this.item == null) {
            return false;
        }
        return !this.item.isValid();
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        Util.ensureThread(false);
        if (!(entity instanceof Item)) {
            return false;
        }
        return AbstractDisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack());
    }

    @Override
    public void fixDisplayMoved() {
        Util.ensureThread(false);
        Location location = this.getDisplayLocation();
        if (this.item != null && location != null) {
            this.item.teleport(location);
            return;
        }
        fixDisplayMovedOld();
    }

    public void fixDisplayMovedOld() {
        Util.ensureThread(false);
        for (Entity entity : Objects.requireNonNull(this.shop.getLocation().getWorld())
                .getEntities()) {
            if (!(entity instanceof Item eItem)) {
                continue;
            }
            if (eItem.getUniqueId().equals(Objects.requireNonNull(this.item).getUniqueId())) {
                if (shop.isLeftShop()) {
                    return;
                }
                Util.debugLog("Fixing moved Item displayItem " + eItem.getUniqueId() + " at " + eItem.getLocation());
                PaperLib.teleportAsync(entity, Objects.requireNonNull(getDisplayLocation()), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                return;
            }
        }
    }

    @Override
    public void fixDisplayNeedRegen() {
        Util.ensureThread(false);
        respawn();
    }

    @Override
    public void remove() {
        Util.ensureThread(false);
        if (this.item == null) {
            Util.debugLog("Ignore the Item removing because the Item is already gone or it's a left shop.");
            return;
        }
        this.item.remove();
        this.item = null;
        this.guardedIstack = null;
        ShopDisplayItemDespawnEvent shopDisplayItemDespawnEvent = new ShopDisplayItemDespawnEvent(
                shop, originalItemStack, DisplayType.REALITEM);
        PLUGIN.getServer().getPluginManager().callEvent(shopDisplayItemDespawnEvent);
    }

    @Override
    public boolean removeDupe() {
        Util.ensureThread(false);
        if (shop.isLeftShop()) {
            return false;
        }
        if (this.item == null) {
            Util.debugLog("Warning: Trying to removeDupe for a null display shop.");
            return false;
        }

        boolean removed = false;

        List<Entity> elist = new ArrayList<>(item.getNearbyEntities(1.5, 1.5, 1.5));
        if (shop.isRealDouble()) {
            elist.addAll(item.getWorld()
                    .getNearbyEntities(Objects.requireNonNull(getDoubleShopDisplayLocations(true)), 1.5,
                            1.5, 1.5));
            elist.addAll(item.getWorld()
                    .getNearbyEntities(Objects.requireNonNull(getDoubleShopDisplayLocations(false)),
                            1.5, 1.5, 1.5));
        }

        for (Entity entity : elist) {
            if (entity.getType() != EntityType.DROPPED_ITEM) {
                continue;
            }
            Item eItem = (Item) entity;
            UUID displayUUID = this.item.getUniqueId();
            if (!eItem.getUniqueId().equals(displayUUID)) {
                if (AbstractDisplayItem.checkIsTargetShopDisplay(eItem.getItemStack(), this.shop)) {
                    Util.debugLog("Removing a duped ItemEntity " + eItem.getUniqueId() + " at " + eItem.getLocation());
                    entity.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public void respawn() {
        Util.ensureThread(false);
        remove();
        spawn();
    }

    @Override
    public void safeGuard(@NotNull Entity entity) {
        Util.ensureThread(false);
        if (!(entity instanceof Item itemEntity)) {
            Util.debugLog(
                    "Failed to safeGuard " + entity.getLocation() + ", cause target not a Item");
            return;
        }
        // Set item protect in the armorstand's hand

        if (PLUGIN.getConfig().getBoolean("shop.display-item-use-name")) {
            QuickShop.getInstance().getPlatform().setDisplayName(itemEntity, Util.getItemStackName(this.originalItemStack));
            itemEntity.setCustomNameVisible(true);
        } else {
            itemEntity.setCustomNameVisible(false);
        }
        itemEntity.setPickupDelay(Integer.MAX_VALUE);
        itemEntity.setSilent(true);
        itemEntity.setInvulnerable(true);
        itemEntity.setPortalCooldown(Integer.MAX_VALUE);
        // TODO: Remove method check when dropping 1.18 and 1.18.1 supports
        if(Util.isMethodAvailable("org.bukkit.entity.Item","setUnlimitedLifetime")) {
            itemEntity.setUnlimitedLifetime(true);
            Util.debugLog("Guard display "+itemEntity+" with 1.18.2+ new unlimited life time api.");
        }
        itemEntity.setVelocity(new Vector(0, 0.1, 0));
    }

    @Override
    public void spawn() {
        Util.ensureThread(false);
        if (shop.isLeftShop()) {
            return;
        }
        if (shop.isDeleted() || !shop.isLoaded()) {
            return;
        }
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Canceled the displayItem spawning because the location in the world is null.");
            return;
        }

        if (originalItemStack == null) {
            Util.debugLog("Canceled the displayItem spawning because the ItemStack is null.");
            return;
        }
        if (item != null && item.isValid()) {
            Util.debugLog("Warning: Spawning the Dropped Item for DisplayItem when there is already an existing Dropped Item, May cause a duplicated Dropped Item!");
            MsgUtil.debugStackTrace(Thread.currentThread().getStackTrace());
        }
        if (!Util.isDisplayAllowBlock(
                Objects.requireNonNull(getDisplayLocation()).getBlock().getType())) {
            Util.debugLog("Can't spawn the displayItem because there is not an AIR block above the shopblock.");
            return;
        }

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop,
                originalItemStack, DisplayType.REALITEM);
        PLUGIN.getServer().getPluginManager().callEvent(shopDisplayItemSpawnEvent);

        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog("Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }
        this.guardedIstack = AbstractDisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
        this.item = this.shop.getLocation().getWorld().dropItem(getDisplayLocation(), this.guardedIstack);
        safeGuard(this.item);
    }

    @Override
    public @Nullable Entity getDisplay() {
        return this.item;
    }

    /**
     * Gets either the item spawn location of this item's chest, or the attached chest.
     * Used for checking for duplicates.
     *
     * @param thisItem Whether to check this item's spawn location or the attached chest's.
     * @return The display location of the item.
     */
    public @Nullable Location getDoubleShopDisplayLocations(boolean thisItem) {
        Util.ensureThread(false);
        if (!shop.isRealDouble()) {
            return null;
        }
        if (thisItem) {
            return shop.getLocation().clone().add(0.5, 1.2, 0.5);
        } else {
            return shop.getAttachedShop().getLocation().clone().add(0.5, 1.2, 0.5);
        }
    }

    @Override
    public boolean isSpawned() {
        if (this.item == null) {
            return false;
        }
        // If it's a left shop, check the attached shop's item instead.
        if (shop.isLeftShop()) {
            Shop attachedShop = shop.getAttachedShop();
            if (attachedShop instanceof ContainerShop shop) {
                if (shop.getDisplayItem() == null) {
                    return false;
                }
                return shop.getDisplayItem().isSpawned();
            }

        }
        return this.item.isValid();
    }

}
