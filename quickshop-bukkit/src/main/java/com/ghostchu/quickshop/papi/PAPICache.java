/*
 *  This file is a part of project QuickShop, the name is PAPICache.java
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

package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PAPICache implements Reloadable {
    private QuickShop plugin;
    private long expiredTime;
    private Cache<String, String> performCaches;

    public PAPICache() {
        init();
        QuickShop.getInstance().getReloadManager().register(this);
    }

    private void init() {
        this.plugin = QuickShop.getInstance();
        this.expiredTime = 15 * 60 * 1000;
        this.performCaches = CacheBuilder.newBuilder()
                .expireAfterWrite(expiredTime, java.util.concurrent.TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    public @NotNull CacheStats getStats() {
        return performCaches.stats();
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void writeCache(@NotNull UUID player, @NotNull String queryString, @NotNull String queryValue) {
        performCaches.put(compileUniqueKey(player, queryString), queryValue);
    }

    @Nullable
    public String readCache(@NotNull UUID player, @NotNull String queryString) {
        return performCaches.getIfPresent(compileUniqueKey(player, queryString));
    }
    
    public String getCached(@NotNull UUID player, @NotNull String[] args) {
        try {
            return performCaches.get(compileUniqueKey(player, args), () -> getValue(player, args));
        } catch (ExecutionException ex) {
            return null;
        }
    }
    
    @NotNull
    private String compileUniqueKey(@NotNull UUID player, @NotNull String[] args) {
        return compileUniqueKey(player, String.join("_", args));
    }
    
    @NotNull
    private String compileUniqueKey(@NotNull UUID player, @NotNull String queryString) {
        return JsonUtil.standard().toJson(new CompiledUniqueKey(player, queryString));
    }
    
    private String getValue(@NotNull UUID player, String[] original) {
        // Make a copy with not-present values being null.
        String[] args = Arrays.copyOf(original, 3);
        
        // Invalid placeholder (%qs_%). Shouldn't happen at all, but you never know...
        if (Util.isNullOrEmpty(args[0]))
            return null;
        
        switch(args[0].toLowerCase(Locale.ROOT)) {
            // %qs_shops-total[_world]%
            case "shops-total" -> {
                // %qs_shops-total%
                if (Util.isNullOrEmpty(args[1]))
                    return String.valueOf(plugin.getShopManager().getAllShops().size());
                
                // %qs_shops-total_<world>%
                return String.valueOf(getShopsInWorld(args[1], false));
            }
            
            // %qs_shops-loaded[_world]%
            case "shops-loaded" -> {
                //%qs_shops-loaded%
                if (Util.isNullOrEmpty(args[1]))
                    return String.valueOf(plugin.getShopManager().getLoadedShops().size());
                
                // %qs_shops-loaded_<world>%
                return String.valueOf(getShopsInWorld(args[1], true));
            }
            
            // %qs_default-currency%
            case "default-currency" -> {
                return plugin.getCurrency();
            }
            
            case "player" -> {
                // Invalid placeholder (%qs_player_%)
                if (Util.isNullOrEmpty(args[1]))
                    return null;
                
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    // %qs_player_shops-total[_uuid]%
                    case "shops-total" -> {
                        // %qs_player_shops-total%
                        if (Util.isNullOrEmpty(args[2]))
                            return String.valueOf(plugin.getShopManager().getPlayerAllShops(player));
                        
                        // Not valid UUID provided
                        if (!Util.isUUID(args[2]))
                            return null;
                        
                        // %qs_player_shop-total_<uuid>%
                        return String.valueOf(plugin.getShopManager().getPlayerAllShops(UUID.fromString(args[2])));
                    }
                    
                    // %qs_player_shops-loaded[_uuid]%
                    case "shops-loaded" -> {
                        // %qs_shops-loaded%
                        if (Util.isNullOrEmpty(args[2]))
                            return String.valueOf(getLoadedPlayerShops(player));
                        
                        // Not valid UUID provided
                        if (!Util.isUUID(args[2]))
                            return null;
                        
                        // %qs_shops-loaded_<uuid>%
                        return String.valueOf(getLoadedPlayerShops(UUID.fromString(args[2])));
                    }
                    
                    // Unknown QS player placeholder
                    default -> {
                        return null;
                    }
                }
            }
            
            // Unknown QS placeholder
            default -> {
                return null;
            }
        }
    }
    
    private long getShopsInWorld(@NotNull String world, boolean loadedOnly) {
        return plugin.getShopManager().getAllShops().stream()
            .filter(shop -> shop.getLocation().getWorld() != null)
            .filter(shop -> shop.getLocation().getWorld().getName().equals(world))
            .filter(shop -> !loadedOnly || shop.isLoaded())
            .count();
    }
    
    private long getLoadedPlayerShops(@NotNull UUID uuid) {
        return plugin.getShopManager().getPlayerAllShops(uuid).stream()
            .filter(Shop::isLoaded)
            .count();
    }
    
    @AllArgsConstructor
    @Data
    static class CompiledUniqueKey {
        private UUID player;
        private String queryString;
    }
}
