/*
 *  This file is a part of project QuickShop, the name is Bentobox.java
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

package com.ghostchu.quickshop.compatibility.bentobox;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Bentobox extends JavaPlugin implements Listener {
    private QuickShopAPI api;
    private boolean deleteShopOnLeave;
    private boolean deleteShopOnReset;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        init();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("QuickShop Compatibility Module - BentoBox loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void init() {
        reloadConfig();
        deleteShopOnLeave = getConfig().getBoolean("delete-shop-on-member-leave");
        deleteShopOnReset = getConfig().getBoolean("delete-shop-on-island-reset");
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuickShopReloading(QSConfigurationReloadEvent event) {
        init();
        getLogger().info("QuickShop Compatibility Module - BentoBox reloaded");
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandResetted(IslandResettedEvent event) {
        if (!deleteShopOnReset)
            return;
        getShops(event.getOldIsland()).forEach(Shop::delete);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandDeleted(IslandDeletedEvent event) {
        if (!deleteShopOnReset)
            return;
        getShops(event.getDeletedIslandInfo()).forEach(Shop::delete);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandKick(world.bentobox.bentobox.api.events.team.TeamKickEvent event) {
        if (!deleteShopOnLeave)
            return;
        getShops(event.getIsland()).forEach((shop) -> {
            if (shop.getOwner().equals(event.getPlayerUUID()))
                shop.delete();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onIslandLeave(world.bentobox.bentobox.api.events.team.TeamLeaveEvent event) {
        if (!deleteShopOnLeave)
            return;
        getShops(event.getIsland()).forEach((shop) -> {
            if (shop.getOwner().equals(event.getPlayerUUID()))
                shop.delete();
        });
    }

    private List<Shop> getShops(IslandDeletion island) {
        List<Shop> shopsList = new ArrayList<>();
        for (int x = island.getMinX() >> 4;
             x <= island.getMaxX() >> 4; x++) {
            for (int z = island.getMinZ() >> 4;
                 z <= island.getMaxZ() >> 4; z++) {
                Map<Location, Shop> shops = this.api.getShopManager().getShops(island.getWorld().getName(), x, z);
                if (shops != null) {
                    shopsList.addAll(shops.values());
                }
            }
        }
        return shopsList;
    }

    private List<Shop> getShops(Island island) {
        List<Shop> shopsList = new ArrayList<>();
        for (int x = island.getMinX() >> 4;
             x <= island.getMaxX() >> 4; x++) {
            for (int z = island.getMinZ() >> 4;
                 z <= island.getMaxZ() >> 4; z++) {
                Map<Location, Shop> shops = this.api.getShopManager().getShops(island.getWorld().getName(), x, z);
                if (shops != null) {
                    shopsList.addAll(shops.values());
                }
            }
        }
        return shopsList;
    }
}
