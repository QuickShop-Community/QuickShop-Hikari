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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main extends CompatibilityModule implements Listener {
    private QuickShopAPI api;
    private boolean whiteList;
    private boolean deleteUntrusted;
    private QuickshopCreateFlag createFlag;
    private QuickshopTradeFlag tradeFlag;

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();
        this.createFlag = new QuickshopCreateFlag();
        this.tradeFlag = new QuickshopTradeFlag();
        GlobalFlagContainer.getInstance().addAll(Arrays.asList(createFlag, tradeFlag));
        getLogger().info(ChatColor.GREEN + getName() + " flags register successfully.");
        PlotSquared.get().getEventDispatcher().registerListener(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        super.onDisable();
        PlotSquared.get().getEventDispatcher().unregisterListener(this);
    }

    public void init() {
        this.whiteList = getConfig().getBoolean("whitelist-mode");
        this.deleteUntrusted = getConfig().getBoolean("delete-when-user-untrusted");
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(shopLoc.getWorld().getName(), shopLoc.getBlockX(), shopLoc.getBlockY(), shopLoc.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) return;
        if (plot.getOwners().contains(event.getAuthorizer())) {
            if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                event.setResult(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void canCreateShopHere(ShopPreCreateEvent event) {
        Location location = event.getLocation();
        com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            if (!whiteList) {
                event.setCancelled(true, Component.text("PlotSquared-Compat: WhiteList Mode is on and no plot found in this position."));
                return;
            }
            return;
        }
        if (!plot.getFlag(tradeFlag)) {
            event.setCancelled(true, Component.text("PlotSquared-Compat: Trade Flag is not enabled."));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void canTradeShopHere(ShopPurchaseEvent event) {
        Location location = event.getShop().getLocation();
        com.plotsquared.core.location.Location pLocation = com.plotsquared.core.location.Location.at(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            if (!whiteList) {
                event.setCancelled(true, Component.text("PlotSquared-Compat: WhiteList Mode is on and no plot found in this position."));
                return;
            }
            return;
        }
        if (!plot.getFlag(tradeFlag)) {
            event.setCancelled(true, Component.text("PlotSquared-Compat: Trade Flag is not enabled."));
        }
    }

    private List<Shop> getShops(Plot plot) {
        List<Shop> shopsList = new ArrayList<>();
        for (CuboidRegion region : plot.getRegions()) {
            shopsList.addAll(getShops(region.getWorld().getName(), region.getMinimumPoint().getX(), region.getMinimumPoint().getZ(), region.getMaximumPoint().getX(), region.getMaximumPoint().getZ()));
        }
        return shopsList;
    }

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent event) {
        getShops(event.getPlot()).forEach(shop -> {
            recordDeletion(event.getPlot().getOwner(), shop, "Plot deleted");
            shop.delete();
        });
    }

    @Subscribe
    public void onPlotPlayerUntrusted(com.plotsquared.core.events.PlayerPlotTrustedEvent event) {
        if (!deleteUntrusted) {
            return;
        }
        if (event.wasAdded()) {
            return; // We only check untrusted
        }
        getShops(event.getPlot()).stream().filter(shop -> shop.getOwner().equals(event.getPlayer())).forEach(shop -> {
            recordDeletion(event.getPlot().getOwner(), shop, "Untrusted -> " + event.getPlayer());
            shop.delete();
        });
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
