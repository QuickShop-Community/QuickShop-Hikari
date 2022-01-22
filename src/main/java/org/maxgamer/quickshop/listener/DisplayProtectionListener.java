/*
 * This file is a part of project QuickShop, the name is DisplayProtectionListener.java
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

package org.maxgamer.quickshop.listener;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.DisplayType;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

public class DisplayProtectionListener extends AbstractProtectionListener {

    private final boolean useEnhanceProtection;

    public DisplayProtectionListener(QuickShop plugin, Cache cache) {
        super(plugin, cache);
        useEnhanceProtection = plugin.getConfig().getBoolean("shop.enchance-display-protect");
        if (useEnhanceProtection) {
            plugin.getServer().getPluginManager().registerEvents(new EnhanceDisplayProtectionListener(plugin, cache), plugin);
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        if (useEnhanceProtection == plugin.getConfig().getBoolean("shop.enchance-display-protect")) {
            return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
        }
        return ReloadResult.builder().status(ReloadStatus.REQUIRE_RESTART).build();
    }


    private void sendAlert(@NotNull String msg) {
        if (!plugin.getConfig().getBoolean("send-display-item-protection-alert")) {
            return;
        }
        MsgUtil.sendGlobalAlert(msg);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void portal(EntityPortalEvent event) {
        if (AbstractDisplayItem.getNowUsing() != DisplayType.REALITEM) {
            return;
        }
        if (!(event.getEntity() instanceof Item)) {
            return;
        }
        if (AbstractDisplayItem.checkIsGuardItemStack(((Item) event.getEntity()).getItemStack())) {
            event.setCancelled(true);
            event.getEntity().remove();
            sendAlert(
                    "[DisplayGuard] Somebody want dupe the display by Portal at "
                            + event.getFrom()
                            + " , QuickShop already cancel it.");
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryOpenEvent event) {
        Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        if (AbstractDisplayItem.getNowUsing() != DisplayType.REALITEM) {
            return;
        }
        if (!AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            return; // We didn't care that
        }
        @Nullable Location loc = event.getInventory().getLocation();
        @Nullable InventoryHolder holder = event.getInventory().getHolder();
        event.setCancelled(true);
        sendAlert(
                "[DisplayGuard] Something  "
                        + holder
                        + " at "
                        + loc
                        + " trying pickup the DisplayItem,  you should teleport to that location and to check detail..");
        Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(ItemDespawnEvent event) {
        if (AbstractDisplayItem.getNowUsing() != DisplayType.REALITEM) {
            return;
        }
        final ItemStack itemStack = event.getEntity().getItemStack();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
        }

        // Util.debugLog("We canceled an Item from despawning because they are our display item.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerFishEvent event) {
        if (AbstractDisplayItem.getNowUsing() != DisplayType.REALITEM) {
            return;
        }
        if (event.getState() != State.CAUGHT_ENTITY) {
            return;
        }
        if (event.getCaught() == null) {
            return;
        }
        if (event.getCaught().getType() != EntityType.DROPPED_ITEM) {
            return;
        }
        final Item item = (Item) event.getCaught();
        final ItemStack is = item.getItemStack();
        if (!AbstractDisplayItem.checkIsGuardItemStack(is)) {
            return;
        }
        event.getHook().remove();
        event.setCancelled(true);
        sendAlert(
                "[DisplayGuard] Player "
                        + event.getPlayer().getName()
                        + " trying hook item use Fishing Rod, QuickShop already removed it.");
        Util.inventoryCheck(new BukkitInventoryWrapper(event.getPlayer().getInventory()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerArmorStandManipulateEvent event) {
        if (!AbstractDisplayItem.checkIsGuardItemStack(event.getArmorStandItem())) {
            return;
        }
        if (AbstractDisplayItem.getNowUsing() != DisplayType.REALITEM) {
            return;
        }
        event.setCancelled(true);
        Util.inventoryCheck(new BukkitInventoryWrapper(event.getPlayer().getInventory()));
        sendAlert(
                "[DisplayGuard] Player  "
                        + event.getPlayer().getName()
                        + " trying mainipulate armorstand contains displayItem.");
    }

}
