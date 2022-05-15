/*
 *  This file is a part of project QuickShop, the name is LockListener.java
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

package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.Cache;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LockListener extends AbstractProtectionListener {
    public LockListener(@NotNull final QuickShop plugin, @Nullable final Cache cache) {
        super(plugin, cache);
    }

    @Override
    public void register() {
        if (plugin.getConfig().getBoolean("shop.lock")) {
            super.register();
        } else {
            super.unregister();
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        register();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    /*
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        final Player p = e.getPlayer();
        // If the chest was a chest
        if (Util.canBeShop(b)) {
            final Shop shop = getShopPlayer(b.getLocation(), true);

            if (shop == null) {
                return; // Wasn't a shop
            }
            // If they owned it or have bypass perms, they can destroy it
            if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE)
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                plugin.text().of(p, "no-permission").send();
            }
        } else if (Util.isWallSign(b.getType())) {
            b = Util.getAttached(b);

            if (b == null) {
                return;
            }

            final Shop shop = getShopPlayer(b.getLocation(), false);

            if (shop == null) {
                return;
            }
            // If they're the shop owner or have bypass perms, they can destroy
            // it.
            if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.DELETE)
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                plugin.text().of(p, "no-permission").send();
            }
        }
    }

    /*
     * Listens for sign placement to prevent placing sign for creating protection
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if (!(placedBlock.getState() instanceof Sign)) {
            return;
        }
        Block posShopBlock = Util.getAttached(placedBlock);
        if (posShopBlock == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShopIncludeAttached(posShopBlock.getLocation());
        if (shop == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!shop.playerAuthorize(player.getUniqueId(), BuiltInShopPermission.DELETE)) {
            plugin.text().of(player, "that-is-locked").send();
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {

        final Block b = e.getClickedBlock();

        if (b == null) {
            return;
        }

        if (!Util.canBeShop(b)) {
            return;
        }

        final Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Didn't right click it, we dont care.
        }

        final Shop shop = getShopPlayer(b.getLocation(), true);
        // Make sure they're not using the non-shop half of a double chest.
        if (shop == null) {
            return;
        }

        if (!shop.playerAuthorize(p.getUniqueId(), BuiltInShopPermission.ACCESS_INVENTORY)) {
            if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
                plugin.text().of(p, "bypassing-lock").send();
                return;
            }
            plugin.text().of(p, "that-is-locked").send();
            e.setCancelled(true);
        }
    }

    /*
     * Handles hopper placement
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {

        final Block b = e.getBlock();

        if (b.getType() != Material.HOPPER) {
            return;
        }

        final Player p = e.getPlayer();

        if (!Util.isOtherShopWithinHopperReach(b, p)) {
            return;
        }

        if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
            plugin.text().of(p, "bypassing-lock").send();
            return;
        }

        plugin.text().of(p, "that-is-locked").send();
        e.setCancelled(true);
    }

}
