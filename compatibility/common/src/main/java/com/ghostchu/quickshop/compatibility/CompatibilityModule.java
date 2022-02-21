/*
 *  This file is a part of project QuickShop, the name is CompatibilityModule.java
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

package com.ghostchu.quickshop.compatibility;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class CompatibilityModule extends JavaPlugin implements Listener {
    private QuickShopAPI api;

    @Override
    public void onLoad() {
        try {saveDefaultConfig();}catch (IllegalArgumentException ignored){}
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        getLogger().info("Loading up...");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {saveDefaultConfig();}catch (IllegalArgumentException ignored){}
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Enabling...");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll((Plugin) this);
        getLogger().info("Unloading...");
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onQuickShopReload(QSConfigurationReloadEvent event) {
        reloadConfig();
        init();
        getLogger().info("Reloading configuration...");
    }

    public QuickShopAPI getApi() {
        return api;
    }

    public abstract void init();

    public void recordDeletion(@Nullable UUID uuid, @NotNull Shop shop, @NotNull String reason){
        if(uuid == null)
            uuid = Util.getNilUniqueId();
        this.api.logEvent(new ShopRemoveLog(uuid,reason,shop.saveToInfoStorage()));
    }

    public List<Shop> getShops(@NotNull String worldName, int minX, int minZ,int maxX, int maxZ) {
        List<Shop> shopsList = new ArrayList<>();
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                Map<Location, Shop> shops = this.api.getShopManager().getShops(worldName, x, z);
                if (shops != null) {
                    shopsList.addAll(shops.values());
                }
            }
        }
        return shopsList;
    }

    public List<Shop> getShops(@NotNull String worldName, int chunkX, int chunkZ) {
        List<Shop> shopsList = new ArrayList<>();
        for (int x = chunkX; x <= chunkX; x++) {
            for (int z = chunkZ; z <= chunkZ; z++) {
                Map<Location, Shop> shops = this.api.getShopManager().getShops(worldName, x, z);
                if (shops != null) {
                    shopsList.addAll(shops.values());
                }
            }
        }
        return shopsList;
    }
}
