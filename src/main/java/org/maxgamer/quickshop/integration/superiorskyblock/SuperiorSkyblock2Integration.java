/*
 * This file is a part of project QuickShop, the name is SuperiorSkyblock2Integration.java
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

package org.maxgamer.quickshop.integration.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.logging.container.ShopRemoveLog;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.Map;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class SuperiorSkyblock2Integration extends AbstractQSIntegratedPlugin implements Listener {
    private boolean onlyOwnerCanCreateShop;
    private boolean deleteShopOnMemberLeave;

    public SuperiorSkyblock2Integration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        onlyOwnerCanCreateShop = plugin.getConfig().getBoolean("integration.superiorskyblock.owner-create-only");
        deleteShopOnMemberLeave = plugin.getConfig().getBoolean("integration.superiorskyblock.delete-shop-on-member-leave");
    }

    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @Override
    public @NotNull String getName() {
        return "SuperiorSkyblock";
    }

    /**
     * Check if a player can create shop here
     *
     * @param player   the player want to create shop
     * @param location shop location
     * @return If you can create shop here
     */
    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        if (island == null) {
            return false;
        }
        if (onlyOwnerCanCreateShop) {
            return island.getOwner().equals(superiorPlayer);
        } else {
            if (island.getOwner().equals(superiorPlayer)) {
                return true;
            }
            return island.isMember(superiorPlayer);
        }
    }

    /**
     * Check if a player can trade with shop here
     *
     * @param player   the player want to trade with shop
     * @param location shop location
     * @return If you can trade with shop here
     */
    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return true;
    }

    /**
     * Loading logic
     * Execute Stage defined by IntegrationStage
     */
    @Override
    public void load() {
        if (plugin.getConfig().getBoolean("integration.superiorskyblock.delete-shop-on-member-leave")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    @Override
    public void unload() {
        IslandQuitEvent.getHandlerList().unregister(this);
        IslandKickEvent.getHandlerList().unregister(this);
        IslandChunkResetEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent event) {
        event.getIsland().getAllChunks().forEach((chunk) -> {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null && !shops.isEmpty()) {
                shops.forEach((location, shop) -> {
                    if (shop.getOwner().equals(event.getPlayer().getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(event.getPlayer().getUniqueId(), String.format("[%s Integration]Shop %s deleted caused by ShopOwnerQuitFromIsland", this.getName(), shop), shop.saveToInfoStorage()));
                        shop.delete();
                    }
                });
            }
        });
    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandKickEvent event) {
        event.getIsland().getAllChunks().forEach((chunk) -> {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null && !shops.isEmpty()) {
                shops.forEach((location, shop) -> {
                    if (shop.getOwner().equals(event.getTarget().getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(event.getPlayer().getUniqueId(), String.format("[%s Integration]Shop %s deleted caused by ShopOwnerKickedFromIsland", this.getName(), shop), shop.saveToInfoStorage()));
                        shop.delete();
                    }
                });
            }
        });

    }

    @EventHandler
    public void deleteShops(com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent event) {
        Map<Location, Shop> shops = plugin.getShopManager().getShops(event.getWorld().getName(), event.getChunkX(), event.getChunkZ());
        if (shops != null && !shops.isEmpty()) {
            shops.forEach((location, shop) -> {
                plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), String.format("[%s Integration]Shop %s deleted caused by IslandChunkReset", this.getName(), shop), shop.saveToInfoStorage()));
                shop.delete();
            });
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
