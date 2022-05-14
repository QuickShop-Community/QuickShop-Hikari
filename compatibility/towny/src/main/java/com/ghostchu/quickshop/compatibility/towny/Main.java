/*
 *  This file is a part of project QuickShop, the name is Towny.java
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

package com.ghostchu.quickshop.compatibility.towny;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PlotClearEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main extends CompatibilityModule implements Listener {
    private QuickShopAPI api;
    private List<TownyFlags> createFlags;
    private List<TownyFlags> tradeFlags;
    private boolean ignoreDisabledWorlds;
    private boolean deleteShopOnLeave;
    private boolean deleteShopOnPlotClear;
    private boolean deleteShopOnPlotDestroy;
    private boolean whiteList;

    @Override
    public void init() {
        createFlags = TownyFlags.deserialize(getConfig().getStringList("create"));
        tradeFlags = TownyFlags.deserialize(getConfig().getStringList("trade"));
        ignoreDisabledWorlds = getConfig().getBoolean("ignore-disabled-worlds");
        deleteShopOnLeave = getConfig().getBoolean("delete-shop-on-resident-leave");
        deleteShopOnPlotClear = getConfig().getBoolean("delete-shop-on-plot-clear");
        deleteShopOnPlotDestroy = getConfig().getBoolean("delete-shop-on-plot-destroy");
        whiteList = getConfig().getBoolean("towny.whitelist-mode");
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        Town town = TownyAPI.getInstance().getTown(shopLoc);
        if (town == null) return;
        if (town.getMayor().getUUID().equals(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
            }
            return;
        }
        try {
            Nation nation = town.getNation();
            if (nation.getKing().getUUID().equals(event.getAuthorizer())) {
                if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                    event.setResult(true);
                }
            }
        } catch (NotRegisteredException ignored) {

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        if (checkFlags(event.getPlayer(), event.getLocation(), this.createFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        //noinspection ConstantConditions
        if (checkFlags(event.getPlayer(), event.getShop().getLocation(), this.createFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrading(ShopPurchaseEvent event) {
        //noinspection ConstantConditions
        if (checkFlags(event.getPlayer(), event.getShop().getLocation(), this.tradeFlags)) {
            return;
        }
        event.setCancelled(true, "Towny Blocked");
    }

    public void deleteShops(UUID owner, Town town) {
        if (!deleteShopOnLeave) {
            return;
        }

        if (owner == null) {
            return;
        }
        String worldName = town.getHomeblockWorld().getName();
        //Getting all shop with world-chunk-shop mapping
        for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : api.getShopManager().getShops().entrySet()) {
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
                                        recordDeletion(owner, shop, "Town leaved");
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
        for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : api.getShopManager().getShops().entrySet()) {
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


    private boolean checkFlags(@NotNull Player player, @NotNull Location location, @NotNull List<TownyFlags> flags) {
        if (ignoreDisabledWorlds && !TownyAPI.getInstance().isTownyWorld(location.getWorld())) {
            Log.debug("This world disabled Towny.");
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

    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return checkFlags(player, location, tradeFlags);
    }

}
