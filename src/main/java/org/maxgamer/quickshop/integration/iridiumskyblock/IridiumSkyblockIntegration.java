/*
 * This file is a part of project QuickShop, the name is IridiumSkyblockIntegration.java
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


package org.maxgamer.quickshop.integration.iridiumskyblock;

import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import com.iridium.iridiumskyblock.api.IslandRegenEvent;
import com.iridium.iridiumskyblock.api.UserKickEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
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

import java.util.List;
import java.util.Optional;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class IridiumSkyblockIntegration extends AbstractQSIntegratedPlugin implements Listener {

    private boolean onlyOwnerCanCreateShop;

    public IridiumSkyblockIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        onlyOwnerCanCreateShop = plugin.getConfig().getBoolean("integration.iridiumskyblock.owner-create-only");
    }

    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @Override
    public @NotNull String getName() {
        return "IridiumSkyblock";
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
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(location.getWorld())) {
            return false;
        }
        Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location);
        if (!island.isPresent()) {
            return false;
        }
        if (onlyOwnerCanCreateShop) {
            return island.get().getOwner().getUuid().equals(player.getUniqueId());
        } else {
            if (island.get().getOwner().getUuid().equals(player.getUniqueId())) {
                return true;
            }
            return island.get().getMembers().stream().anyMatch(users -> users.getUuid().equals(player.getUniqueId()));
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
        if (plugin.getConfig().getBoolean("integration.iridiumskyblock.delete-shop-on-member-leave")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    @Override
    public void unload() {
        IslandDeleteEvent.getHandlerList().unregister(this);
        IslandRegenEvent.getHandlerList().unregister(this);
        UserKickEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void deleteShopsWhenIslandDelete(IslandDeleteEvent event) {
        Island island = event.getIsland();
        List<User> members = event.getIsland().getMembers();
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!island.isInIsland(shop.getLocation())) {
                continue;
            }
            for (User user : members) {
                if (shop.getOwner().equals(user.getUuid())) {
                    plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), String.format("[%s Integration]Shop %s deleted caused by ShopOwnerQuitFromIsland", this.getName(), shop), shop.saveToInfoStorage()));
                    shop.delete();
                }
            }
        }
    }

    @EventHandler
    public void deleteShopsWhenIslandDelete(IslandRegenEvent event) {
        Island island = event.getIsland();
        List<User> members = event.getIsland().getMembers();
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (!island.isInIsland(shop.getLocation())) {
                continue;
            }
            for (User user : members) {
                if (shop.getOwner().equals(user.getUuid())) {
                    plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), String.format("[%s Integration]Shop %s deleted caused by ShopOwnerQuitFromIsland", this.getName(), shop), shop.saveToInfoStorage()));
                    shop.delete();
                }
            }
        }
    }

    @EventHandler
    public void deleteShopWhenMemberKicked(UserKickEvent event) {
        Island island = event.getIsland();
        for (Shop shop : plugin.getShopManager().getPlayerAllShops(event.getUser().getUuid())) {
            if (!island.isInIsland(shop.getLocation())) {
                continue;
            }
            plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), String.format("[%s Integration]Shop %s deleted caused by ShopOwnerQuitFromIsland", this.getName(), shop), shop.saveToInfoStorage()));
            shop.delete();
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
