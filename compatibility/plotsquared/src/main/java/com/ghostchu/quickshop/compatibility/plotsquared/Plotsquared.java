/*
 *  This file is a part of project QuickShop, the name is Plotsquared.java
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

package com.ghostchu.quickshop.compatibility.plotsquared;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Plotsquared extends JavaPlugin implements Listener {
    private QuickShopAPI api;
    private boolean whiteList;
    private boolean deleteUntrusted;
    private QuickshopCreateFlag createFlag;
    private QuickshopTradeFlag tradeFlag;
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.api = (QuickShopAPI)Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        init();
        Bukkit.getPluginManager().registerEvents(this,this);
        this.createFlag = new QuickshopCreateFlag();
        this.tradeFlag = new QuickshopTradeFlag();
        GlobalFlagContainer.getInstance().addAll(Arrays.asList(createFlag, tradeFlag));
        getLogger().info(ChatColor.GREEN + getName() + " flags register successfully.");
        Util.debugLog("Success register " + getName() + " flags.");
        PlotSquared.get().getEventDispatcher().registerListener(this);
        getLogger().info("QuickShop Compatibility Module - PlotSquared loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlotSquared.get().getEventDispatcher().unregisterListener(this);
    }


    private void init() {
        reloadConfig();
        this.whiteList = getConfig().getBoolean("whitelist-mode");
        this.deleteUntrusted = getConfig().getBoolean("delete-when-user-untrusted");
    }
    @EventHandler
    public void onQuickShopReloading(QSConfigurationReloadEvent event){
        init();
        getLogger().info("QuickShop Compatibility Module - PlotSquared reloaded");
    }

    @EventHandler(ignoreCancelled = true)
    public boolean canCreateShopHere(ShopPreCreateEvent event) {
        Location location = event.getLocation();
        com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            return !whiteList;
        }
        return plot.getFlag(createFlag);
    }
    @EventHandler(ignoreCancelled = true)
    public boolean canTradeShopHere(ShopPurchaseEvent event) {
        Location location = event.getShop().getLocation();
        com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            return !whiteList;
        }
        return plot.getFlag(tradeFlag);
    }

    private List<Shop> getShops(Plot plot) {
        List<Shop> shopsList = new ArrayList<>();
        for (CuboidRegion region : plot.getRegions()) {
            for (int x = region.getMinimumPoint().getX() >> 4;
                 x <= region.getMaximumPoint().getX() >> 4; x++) {
                for (int z = region.getMinimumPoint().getZ() >> 4;
                     z <= region.getMaximumPoint().getZ() >> 4; z++) {
                    Map<Location, Shop> shops = this.api.getShopManager().getShops(plot.getWorldName(), x, z);
                    if (shops != null) {
                        shopsList.addAll(shops.values());
                    }
                }
            }
        }
        return shopsList;
    }

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        getShops(event.getPlot()).forEach(Shop::delete);
    }

    @Subscribe
    public void onPlotPlayerUntrusted(com.plotsquared.core.events.PlayerPlotTrustedEvent event) {
        if (!deleteUntrusted) {
            return;
        }
        if (event.wasAdded()) {
            return; // We only check untrusted
        }
        getShops(event.getPlot()).stream().filter(shop -> shop.getOwner().equals(event.getPlayer())).forEach(Shop::delete);
    }



    static class QuickshopCreateFlag extends BooleanFlag<QuickshopCreateFlag> {

        protected QuickshopCreateFlag(boolean value, Caption description) {
            super(value, description);
        }

        public QuickshopCreateFlag() {
            super(true, TranslatableCaption.of("quickshop-create"));
        }

        @Override
        protected QuickshopCreateFlag flagOf(@NotNull Boolean aBoolean) {
            return new QuickshopCreateFlag(aBoolean, TranslatableCaption.of("quickshop-create"));
        }
    }

    static class QuickshopTradeFlag extends BooleanFlag<QuickshopTradeFlag> {

        protected QuickshopTradeFlag(boolean value, Caption description) {
            super(value, description);
        }

        public QuickshopTradeFlag() {
            super(true, TranslatableCaption.of("quickshop-trade"));
        }

        @Override
        protected QuickshopTradeFlag flagOf(@NotNull Boolean aBoolean) {
            return new QuickshopTradeFlag(aBoolean, TranslatableCaption.of("quickshop-trade"));
        }
    }

}
