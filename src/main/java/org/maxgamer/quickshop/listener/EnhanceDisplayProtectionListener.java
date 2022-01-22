/*
 * This file is a part of project QuickShop, the name is EnhanceDisplayProtectionListener.java
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

import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

public class EnhanceDisplayProtectionListener extends AbstractProtectionListener implements Listener {

    public EnhanceDisplayProtectionListener(QuickShop plugin, Cache cache) {
        super(plugin, cache);
    }

    private void sendAlert(@NotNull String msg) {
        if (!plugin.getConfig().getBoolean("send-display-item-protection-alert")) {
            return;
        }
        MsgUtil.sendGlobalAlert(msg);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockFromToEvent event) {
        final Block targetBlock = event.getToBlock();
        final Block shopBlock = targetBlock.getRelative(BlockFace.DOWN);
        final Shop shop = getShopNature(shopBlock.getLocation(), true);
        if (shop == null) {
            return;
        }
        event.setCancelled(true);
        if (shop.getDisplay() != null) {
            shop.getDisplay().remove();
        }
        sendAlert(
                "[DisplayGuard] Liuqid "
                        + targetBlock.getLocation()
                        + " trying flow to top of shop, QuickShop already cancel it.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonExtendEvent event) {
        final Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = getShopRedstone(block.getLocation(), true);
        if (shop != null) {
            event.setCancelled(true);
            sendAlert(
                    "[DisplayGuard] Piston  "
                            + event.getBlock().getLocation()
                            + " trying push somethings on the shop top, QuickShop already cancel it.");
            if (shop.getDisplay() != null) {
                shop.getDisplay().remove();
            }
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            final Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = getShopNature(otherBlock.getLocation(), true);
                if (shop != null) {
                    event.setCancelled(true);
                    sendAlert(
                            "[DisplayGuard] Piston  "
                                    + event.getBlock().getLocation()
                                    + " trying push somethings on the shop top, QuickShop already cancel it.");
                    if (shop.getDisplay() != null) {
                        shop.getDisplay().remove();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BlockPistonRetractEvent event) {
        final Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
        Shop shop = getShopNature(block.getLocation(), true);
        if (shop != null) {
            event.setCancelled(true);
            sendAlert(
                    "[DisplayGuard] Piston  "
                            + event.getBlock().getLocation()
                            + " trying pull somethings on the shop top, QuickShop already cancel it.");
            if (shop.getDisplay() != null) {
                shop.getDisplay().remove();
            }
            return;
        }
        for (Block oBlock : event.getBlocks()) {
            final Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
            if (Util.canBeShop(otherBlock)) {
                shop = getShopNature(otherBlock.getLocation(), true);
                if (shop != null) {
                    event.setCancelled(true);
                    sendAlert(
                            "[DisplayGuard] Piston  "
                                    + event.getBlock().getLocation()
                                    + " trying push somethings on the shop top, QuickShop already cancel it.");
                    if (shop.getDisplay() != null) {
                        shop.getDisplay().remove();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(BrewingStandFuelEvent event) {
        final ItemStack itemStack = event.getFuel();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            sendAlert(
                    "[DisplayGuard] Block  "
                            + event.getBlock().getLocation()
                            + " trying fuel the BrewingStand with DisplayItem.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceBurnEvent event) {
        final ItemStack itemStack = event.getFuel();

        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Block furnace = event.getBlock();
            BlockState state = PaperLib.getBlockState(event.getBlock(), false).getState();
            if (state instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace.getState();
                sendAlert("[DisplayGuard] Block  " + event.getBlock().getLocation() + " trying burn with DisplayItem.");
                Util.inventoryCheck(new BukkitInventoryWrapper(furnace1.getInventory()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void block(FurnaceSmeltEvent event) {
        ItemStack itemStack = event.getSource();
        BlockState furnace = PaperLib.getBlockState(event.getBlock(), false).getState();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            if (furnace instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace;
                sendAlert(
                        "[DisplayGuard] Block  "
                                + event.getBlock().getLocation()
                                + " trying smelt with DisplayItem.");
                Util.inventoryCheck(new BukkitInventoryWrapper(furnace1.getInventory()));
            }
            return;
        }
        itemStack = event.getResult();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            if (furnace instanceof Furnace) {
                Furnace furnace1 = (Furnace) furnace;
                sendAlert(
                        "[DisplayGuard] Block  "
                                + event.getBlock().getLocation()
                                + " trying smelt with DisplayItem.");
                Util.inventoryCheck(new BukkitInventoryWrapper(furnace1.getInventory()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entity(EntityPickupItemEvent e) {
        final ItemStack stack = e.getItem().getItemStack();
        if (!AbstractDisplayItem.checkIsGuardItemStack(stack)) {
            return;
        }
        e.setCancelled(true);
        // You shouldn't be able to pick up that...
        e.getItem().remove();
        sendAlert(
                "[DisplayGuard] Entity "
                        + e.getEntity().getName()
                        + " # "
                        + e.getEntity().getLocation()
                        + " pickedup the displayItem, QuickShop already removed it.");

        Entity entity = e.getEntity();
        if (entity instanceof InventoryHolder) {
            Util.inventoryCheck(new BukkitInventoryWrapper(((InventoryHolder) entity).getInventory()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryClickEvent event) {
        if (!AbstractDisplayItem.checkIsGuardItemStack(event.getCurrentItem())) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getLocation() == null) {
            return;
        }
        event.setCancelled(true);

        sendAlert(
                "[DisplayGuard] Inventory "
                        + event.getClickedInventory().getHolder()
                        + " at"
                        + event.getClickedInventory().getLocation()
                        + " was clicked the displayItem, QuickShop already removed it.");
        event.getCurrentItem().setType(Material.AIR);
        event.setResult(Event.Result.DENY);
        Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryDragEvent event) {
        ItemStack itemStack = event.getCursor();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getOldCursor();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventory(InventoryCreativeEvent event) {
        ItemStack itemStack = event.getCursor();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCurrentItem();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void item(PlayerItemHeldEvent e) {
        final ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        final ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
        if (AbstractDisplayItem.checkIsGuardItemStack(stack)) {
            e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            sendAlert(
                    "[DisplayGuard] Player "
                            + e.getPlayer().getName()
                            + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(e.getPlayer().getInventory()));
        }
        if (AbstractDisplayItem.checkIsGuardItemStack(stackOffHand)) {
            e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR, 0));
            // You shouldn't be able to pick up that...
            sendAlert(
                    "[DisplayGuard] Player "
                            + e.getPlayer().getName()
                            + " helded the displayItem, QuickShop already cancelled and removed it.");
            e.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(e.getPlayer().getInventory()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(CraftItemEvent event) {
        ItemStack itemStack;
        itemStack = event.getCurrentItem();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
            return;
        }
        itemStack = event.getCursor();
        if (AbstractDisplayItem.checkIsGuardItemStack(itemStack)) {
            event.setCancelled(true);
            Util.inventoryCheck(new BukkitInventoryWrapper(event.getInventory()));
            sendAlert(
                    "[DisplayGuard] Player  "
                            + event.getWhoClicked().getName()
                            + " trying use DisplayItem crafting.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void player(PlayerBucketEmptyEvent event) {
        final Block waterBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        final Shop shop =
                getShopPlayer(waterBlock.getRelative(BlockFace.DOWN).getLocation(), true);
        if (shop == null) {
            return;
        }
        event.setCancelled(true);
        sendAlert(
                "[DisplayGuard] Player  "
                        + event.getPlayer().getName()
                        + " trying use water to move somethings on the shop top, QuickShop already remove it.");
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
