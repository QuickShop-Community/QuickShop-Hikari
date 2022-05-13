/*
 *  This file is a part of project QuickShop, the name is Main.java
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

package com.ghostchu.quickshop.compatibility.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopDeleteOverrideEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;


public final class Main extends CompatibilityModule implements Listener {
    private boolean onlyOwnerCanCreateShop;
    private boolean deleteShopOnMemberLeave;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void init() {
        onlyOwnerCanCreateShop = getConfig().getBoolean("owner-create-only");
        deleteShopOnMemberLeave = getConfig().getBoolean("delete-shop-on-member-leave");
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandOwnerDeleteCheck(ShopDeleteOverrideEvent event) {
        Island land = SuperiorSkyblockAPI.getIslandAt(event.getShop().getLocation());
        if (land != null) {
            if (event.getRequester().equals(land.getOwner().getUniqueId())) {
                event.setOverrideForAllowed(true);
                Log.debug("Island owner delete check: " + event.getRequester() + " is the owner of " + land.getName() + ", grant shop " + event.getShop() + " delete permission.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(event.getPlayer());
        if (island == null) {
            return;
        }
        if (onlyOwnerCanCreateShop) {
            if (!island.getOwner().equals(superiorPlayer)) {
                event.setCancelled(true, "Only owner can create shop there.");
            }
        } else {
            if (!island.getOwner().equals(superiorPlayer)) {
                if (!island.isMember(superiorPlayer)) {
                    event.setCancelled(true, "Only owner or member can create shop there.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        Island island = SuperiorSkyblockAPI.getIslandAt(event.getShop().getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(event.getCreator());
        if (island == null) {
            return;
        }
        if (onlyOwnerCanCreateShop) {
            if (!island.getOwner().equals(superiorPlayer)) {
                event.setCancelled(true, "Only owner can create shop there.");
            }
        } else {
            if (!island.getOwner().equals(superiorPlayer)) {
                if (!island.isMember(superiorPlayer)) {
                    event.setCancelled(true, "Only owner or member can create shop there.");
                }
            }
        }
    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent event) {
        if (!deleteShopOnMemberLeave)
            return;
        event.getIsland().getAllChunks().forEach((chunk) -> {
            Map<Location, Shop> shops = QuickShop.getInstance().getShopManager().getShops(chunk);
            if (shops != null && !shops.isEmpty()) {
                shops.forEach((location, shop) -> {
                    if (shop.getOwner().equals(event.getPlayer().getUniqueId())) {
                        recordDeletion(event.getPlayer().getUniqueId(), shop, String.format("[%s Integration]Shop %s deleted caused by ShopOwnerQuitFromIsland", this.getName(), shop));
                        ;
                        shop.delete();
                    }
                });
            }
        });
    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandKickEvent event) {
        if (!deleteShopOnMemberLeave)
            return;
        event.getIsland().getAllChunks().forEach((chunk) -> {
            Map<Location, Shop> shops = QuickShop.getInstance().getShopManager().getShops(chunk);
            if (shops != null && !shops.isEmpty()) {
                shops.forEach((location, shop) -> {
                    if (shop.getOwner().equals(event.getTarget().getUniqueId())) {
                        recordDeletion(event.getPlayer().getUniqueId(), shop, String.format("[%s Integration]Shop %s deleted caused by ShopOwnerKickedFromIsland", this.getName(), shop));
                        ;
                        shop.delete();
                    }
                });
            }
        });

    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent event) {
        Map<Location, Shop> shops = QuickShop.getInstance().getShopManager().getShops(event.getWorld().getName(), event.getChunkX(), event.getChunkZ());
        if (shops != null && !shops.isEmpty()) {
            shops.forEach((location, shop) -> {
                recordDeletion(Util.getNilUniqueId(), shop, String.format("[%s Integration]Shop %s deleted caused by IslandChunkReset", this.getName(), shop));
                ;
                shop.delete();
            });
        }
    }
}
