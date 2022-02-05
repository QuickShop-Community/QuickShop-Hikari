/*
 * This file is a part of project QuickShop, the name is ChunkListener.java
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

import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.Map;

public class ChunkListener extends AbstractQSListener {

    public ChunkListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {

        if (e.isNewChunk()) {
            return;
        }
        final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(e.getChunk());
        if (inChunk == null) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Shop shop : inChunk.values()) {
                shop.onLoad();
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(e.getChunk());
        if (inChunk == null) {
            return;
        }
        for (Shop shop : inChunk.values()) {
            if (shop.isLoaded()) {
                shop.onUnload();
            }
        }
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
