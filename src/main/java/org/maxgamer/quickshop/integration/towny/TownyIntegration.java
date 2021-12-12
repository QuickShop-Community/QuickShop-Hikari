/*
 * This file is a part of project QuickShop, the name is TownyIntegration.java
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

package org.maxgamer.quickshop.integration.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopChunk;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class TownyIntegration extends AbstractQSIntegratedPlugin implements Listener {
    private List<TownyFlags> createFlags;

    private List<TownyFlags> tradeFlags;

    private boolean ignoreDisabledWorlds;
    private boolean deleteShopOnLeave;
    private boolean deleteShopOnPlotClear;
    private boolean isNewVersion;
    private boolean whiteList;
    private boolean deleteShopOnPlotDestroy;


    public TownyIntegration(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
        //Testing if there have new method
        try {
            Town.class.getDeclaredMethod("getHomeblockWorld");
            isNewVersion = true;
        } catch (NoSuchMethodException exception) {
            isNewVersion = false;
        }
    }

    private void init() {
        createFlags = TownyFlags.deserialize(plugin.getConfig().getStringList("integration.towny.create"));
        tradeFlags = TownyFlags.deserialize(plugin.getConfig().getStringList("integration.towny.trade"));
        ignoreDisabledWorlds = plugin.getConfig().getBoolean("integration.towny.ignore-disabled-worlds");
        deleteShopOnLeave = plugin.getConfig().getBoolean("integration.towny.delete-shop-on-resident-leave");
        deleteShopOnPlotClear = plugin.getConfig().getBoolean("integration.towny.delete-shop-on-plot-clear");
        deleteShopOnPlotDestroy = plugin.getConfig().getBoolean("integration.towny.delete-shop-on-plot-destroy");
        whiteList = plugin.getConfig().getBoolean("integration.towny.whitelist-mode");
    }

    @Override
    public @NotNull String getName() {
        return "Towny";
    }

    public void deleteShops(UUID owner, Town town) {
        if (!deleteShopOnLeave) {
            return;
        }

        if (owner == null) {
            return;
        }
        String worldName;
        if (isNewVersion) {
            worldName = town.getHomeblockWorld().getName();
        } else {
            worldName = town.getWorld().getName();
        }
        //Getting all shop with world-chunk-shop mapping
        for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : plugin.getShopManager().getShops().entrySet()) {
            //Matching world
            if (worldName.equals(entry.getKey())) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world != null) {
                    //Matching Location
                    for (Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {
                        Map<Location, Shop> shopMap = chunkedShopEntry.getValue();
                        for (Shop shop : shopMap.values()) {
                            //Matching Owner
                            if (shop.getOwner().equals(owner)) {
                                try {
                                    //It should be equal in address
                                    if (WorldCoord.parseWorldCoord(shop.getLocation()).getTownBlock().getTown() == town) {
                                        //delete it
                                        shop.delete();
                                    }
                                } catch (NotRegisteredException ignored) {
                                    //Is not in town, continue
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void purgeShops(TownBlock townBlock) {
        purgeShops(townBlock.getWorldCoord());
    }

    public void purgeShops(WorldCoord worldCoord) {
        if (worldCoord == null) {
            return;
        }
        String worldName;
        worldName = worldCoord.getBukkitWorld().getName();
        //Getting all shop with world-chunk-shop mapping
        for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : plugin.getShopManager().getShops().entrySet()) {
            //Matching world
            if (worldName.equals(entry.getKey())) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world != null) {
                    //Matching Location
                    for (Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {
                        Map<Location, Shop> shopMap = chunkedShopEntry.getValue();
                        for (Shop shop : shopMap.values()) {
                            //Matching Owner
                            if (WorldCoord.parseWorldCoord(shop.getLocation()).equals(worldCoord)) {
                                //delete it
                                shop.delete();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(TownRemoveResidentEvent event) {
        if (Bukkit.isPrimaryThread()) {
            deleteShops(TownyAPI.getInstance().getPlayerUUID(event.getResident()), event.getTown());
        } else {
            Util.mainThreadRun(() -> deleteShops(TownyAPI.getInstance().getPlayerUUID(event.getResident()), event.getTown()));
        }
    }

    @EventHandler
    public void onPlotClear(PlotClearEvent event) {
        if (!deleteShopOnPlotClear) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            purgeShops(event.getTownBlock());
        } else {
            Util.mainThreadRun(() -> purgeShops(event.getTownBlock()));
        }
    }

    @EventHandler
    public void onPlotDestroy(TownUnclaimEvent event) {
        if (!deleteShopOnPlotDestroy) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            purgeShops(event.getWorldCoord());
        } else {
            Util.mainThreadRun(() -> purgeShops(event.getWorldCoord()));
        }
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        return checkFlags(player, location, createFlags);
    }

    private boolean checkFlags(@NotNull Player player, @NotNull Location location, List<TownyFlags> flags) {
        if (ignoreDisabledWorlds && !TownyAPI.getInstance().isTownyWorld(location.getWorld())) {
            Util.debugLog("This world disabled Towny.");
            return true;
        }
        if (!whiteList && !ShopPlotUtil.isShopPlot(location)) {
            return true;
        }
        for (TownyFlags flag : flags) {
            switch (flag) {
                case OWN:
                    if (!ShopPlotUtil.doesPlayerOwnShopPlot(player, location)) {
                        return false;
                    }
                    break;
                case MODIFY:
                    if (!ShopPlotUtil.doesPlayerHaveAbilityToEditShopPlot(player, location)) {
                        return false;
                    }
                    break;
                case SHOPTYPE:
                    if (!ShopPlotUtil.isShopPlot(location)) {
                        return false;
                    }
                default:
                    // Ignore
            }
        }
        return true;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return checkFlags(player, location, tradeFlags);
    }

    @Override
    public void load() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}