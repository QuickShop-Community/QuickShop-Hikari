/*
 * This file is a part of project QuickShop, the name is AbstractProtectionListener.java
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

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;

@Getter
public abstract class AbstractProtectionListener extends AbstractQSListener {
    private final Cache cache;

    public AbstractProtectionListener(@NotNull QuickShop plugin, @Nullable Cache cache) {
        super(plugin);
        plugin.getReloadManager().register(this);
        this.cache = cache;
    }

    public QuickShop getPlugin() {
        return plugin;
    }

    /**
     * Get shop for redstone events, will caching if caching enabled
     *
     * @param location        The block location
     * @param includeAttached whether to include the attached shop
     * @return The shop object
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Nullable
    public Shop getShopRedstone(@NotNull Location location, boolean includeAttached) {
        if (cache != null) {
            return cache.find(location, includeAttached);
        } else {
            if (includeAttached) {
                return plugin.getShopManager().getShopIncludeAttached(location);
            } else {
                return plugin.getShopManager().getShop(location);
            }
        }
    }

    /**
     * Get shop for player events, won't be caching
     *
     * @param location        The block location
     * @param includeAttached whether to include the attached shop
     * @return The shop object
     */
    @Nullable
    public Shop getShopPlayer(@NotNull Location location, boolean includeAttached) {
        return includeAttached ? plugin.getShopManager().getShopIncludeAttached(location, false) : plugin.getShopManager().getShop(location);
    }

    /**
     * Get shop for nature events, may will caching but usually it doesn't will cached.
     * Because nature events usually won't check same block twice in shore time.
     *
     * @param location        The block location
     * @param includeAttached whether to include the attached shop
     * @return The shop object
     */
    @Nullable
    public Shop getShopNature(@NotNull Location location, boolean includeAttached) {
        return includeAttached ? plugin.getShopManager().getShopIncludeAttached(location, false) : plugin.getShopManager().getShop(location);
    }

}
