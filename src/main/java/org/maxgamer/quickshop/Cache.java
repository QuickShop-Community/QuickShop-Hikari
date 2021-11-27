/*
 * This file is a part of project QuickShop, the name is Cache.java
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

package org.maxgamer.quickshop;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.shop.SimpleShopManager;

import java.util.concurrent.TimeUnit;

/**
 * Cache is a utilities to quick access shops on large network with Caffeine Cache Library
 *
 * @author Ghost_chu
 */
public class Cache {
    private final QuickShop plugin;
    private final com.google.common.cache.Cache<Location, BoxedShop> accessCaching = CacheBuilder
            .newBuilder()
            .initialCapacity(5000)
            .expireAfterAccess(120, TimeUnit.MINUTES)
            .recordStats()
            .build();

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
    }

    public @NotNull CacheStats getStats() {
        return accessCaching.stats();
    }


    /**
     * Gets shop from plugin caching
     *
     * @param location The shop location that you want to get
     * @param attached Does search for attached
     * @return The shop, null for no shops found in caching and memory
     */
    @Nullable
    public Shop find(@NotNull Location location, boolean attached) {
        BoxedShop boxedShop = accessCaching.getIfPresent(location);
        if (boxedShop == null) {
            if (attached) {
                boxedShop = new BoxedShop(((SimpleShopManager) plugin.getShopManager()).findShopIncludeAttached(location, false));
            } else {
                boxedShop = new BoxedShop(plugin.getShopManager().getShop(location));
            }
        }
        setCache(location, boxedShop.getShop());
        return boxedShop.getShop();
    }

    /**
     * Update and invalidate the caching
     *
     * @param location The location that you want to update
     * @param shop     null for invalidate and Shop object for update
     */
    public void setCache(@NotNull Location location, @Nullable Shop shop) {
        accessCaching.put(location, new BoxedShop(shop));
    }

    public void invalidate(@NotNull Location location) {
        accessCaching.invalidate(location);
    }

    @AllArgsConstructor
    @Data
    static class BoxedShop {
        private Shop shop;

        public boolean isPresent() {
            return shop != null;
        }
    }
}
