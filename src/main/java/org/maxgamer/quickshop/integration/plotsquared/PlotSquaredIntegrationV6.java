/*
 * This file is a part of project QuickShop, the name is PlotSquaredIntegrationV6.java
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

package org.maxgamer.quickshop.integration.plotsquared;/*
 * This file is a part of project QuickShop, the name is PlotSquaredIntegrationV5.java
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

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegrateStage;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class PlotSquaredIntegrationV6 extends AbstractQSIntegratedPlugin {
    private boolean whiteList;
    private boolean deleteUntrusted;
    private QuickshopCreateFlag createFlag;
    private QuickshopTradeFlag tradeFlag;

    public PlotSquaredIntegrationV6(QuickShop plugin) {
        super(plugin);
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() {
        this.whiteList = plugin.getConfig().getBoolean("integration.plotsquared.whitelist-mode");
        this.deleteUntrusted = plugin.getConfig().getBoolean("integration.plotsquared.delete-when-user-untrusted");
    }

    @Override
    public @NotNull String getName() {
        return "PlotSquared";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
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

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        com.plotsquared.core.location.Location pLocation =
                com.plotsquared.core.location.Location.at(
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

    @Override
    public void load() {
        this.createFlag = new QuickshopCreateFlag();
        this.tradeFlag = new QuickshopTradeFlag();
        GlobalFlagContainer.getInstance().addAll(Arrays.asList(createFlag, tradeFlag));
        plugin.getLogger().info(ChatColor.GREEN + getName() + " flags register successfully.");
        Util.debugLog("Success register " + getName() + " flags.");
        PlotSquared.get().getEventDispatcher().registerListener(this);
    }

    @Override
    public void unload() {
        PlotSquared.get().getEventDispatcher().unregisterListener(this);
    }

    private List<Shop> getShops(Plot plot) {
        List<Shop> shopsList = new ArrayList<>();
        for (CuboidRegion region : plot.getRegions()) {
            for (int x = region.getMinimumPoint().getX() >> 4;
                 x <= region.getMaximumPoint().getX() >> 4; x++) {
                for (int z = region.getMinimumPoint().getZ() >> 4;
                     z <= region.getMaximumPoint().getZ() >> 4; z++) {
                    Map<Location, Shop> shops = plugin.getShopManager().getShops(plot.getWorldName(), x, z);
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

