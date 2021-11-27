/*
 * This file is a part of project QuickShop, the name is DisplayAutoDespawnWatcher.java
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

package org.maxgamer.quickshop.watcher;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

@AllArgsConstructor
public class DisplayAutoDespawnWatcher extends BukkitRunnable implements Reloadable {
    private final QuickShop plugin;
    private int range;

    public DisplayAutoDespawnWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);

    }

    private void init() {
        this.range = plugin.getConfiguration().getInt("shop.display-despawn-range");
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    @Override
    public void run() {
        for (Shop shop : plugin.getShopManager().getLoadedShops()) {
            //Shop may deleted or unloaded when iterating
            if (shop.isDeleted() || !shop.isLoaded()) {
                continue;
            }
            World world = shop.getLocation().getWorld(); //Cache this, because it will took some time.
            if (shop.getDisplay() != null) {
                // Check the range has player?
                boolean anyPlayerInRegion = false;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if ((player.getWorld() == world) && (player.getLocation().distance(shop.getLocation()) < range)) {
                        anyPlayerInRegion = true;
                        break;
                    }
                }
                if (anyPlayerInRegion) {
                    if (!shop.getDisplay().isSpawned()) {
                        Util.debugLog(
                                "Respawning the shop "
                                        + shop
                                        + " the display, cause it was despawned and a player close to it");
                        Util.mainThreadRun(() -> shop.getDisplay().spawn());
                    }
                } else if (shop.getDisplay().isSpawned()) {
                    removeDisplayItemDelayed(shop);
                }
            }
        }
    }

    public boolean removeDisplayItemDelayed(Shop shop) {
        if (shop.getDisplay() != null) {
            if (shop.getDisplay().isPendingRemoval()) {
                // Actually remove the pending display
                //Util.debugLog("Removing the shop " + shop + " the display, cause nobody can see it");
                Util.mainThreadRun(() -> shop.getDisplay().remove());
                return true;
            } else {
                // Delayed to next calling
                //Util.debugLog("Pending to remove the shop " + shop + " the display, cause nobody can see it");
                shop.getDisplay().pendingRemoval();
                return false;
            }
        }
        return false;
    }

}
