///*
// * This file is a part of project QuickShop, the name is PlotSquaredIntegrationProxy.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.maxgamer.quickshop.integration.plotsquared;
//
//import org.bukkit.Location;
//import org.bukkit.entity.Player;
//import org.jetbrains.annotations.NotNull;
//import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.api.integration.IntegrateStage;
//import org.maxgamer.quickshop.api.integration.IntegrationStage;
//import org.maxgamer.quickshop.integration.AbstractQSIntegratedPlugin;
//import com.ghostchu.simplereloadlib.ReloadResult;
//
//@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
//public class PlotSquaredIntegrationProxy extends AbstractQSIntegratedPlugin {
//    private final AbstractQSIntegratedPlugin plotSquared;
//
//    public PlotSquaredIntegrationProxy(QuickShop instance) {
//        super(instance);
//        if (plugin.getServer().getPluginManager().getPlugin("PlotSquared").getClass().getPackage().getName().contains("intellectualsite")) {
//            //    plotSquared = new PlotSquaredIntegrationV4(plugin);
//            // } else if (false && Util.isClassAvailable("com.plotsquared.core.configuration.Caption")) {
////                //Write reload logic for it
////                plotSquared = new PlotSquaredIntegrationV5(plugin) {
////                    @Override
////                    public ReloadResult reloadModule() throws Exception {
////                        loadConfiguration();
////                        return super.reloadModule();
////                    }
////                };
////            plugin.getReloadManager().register(plotSquared);
//            //} else {
//            plotSquared = new PlotSquaredIntegrationV6(plugin);
//        } else {
//            plotSquared = null;
//        }
//        //}
//    }
//
//    @Override
//    public @NotNull String getName() {
//        return plotSquared.getName();
//    }
//
//    @Override
//    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
//        return plotSquared.canCreateShopHere(player, location);
//    }
//
//    @Override
//    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
//        return plotSquared.canCreateShopHere(player, location);
//    }
//
//    @Override
//    public boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
//        return plotSquared.canDeleteShopHere(player, location);
//    }
//
//    @Override
//    public void load() {
//        plotSquared.load();
//    }
//
//    @Override
//    public void unload() {
//        plotSquared.unload();
//    }
//
//    @Override
//    public ReloadResult reloadModule() throws Exception {
//        return plotSquared.reloadModule();
//    }
//}
