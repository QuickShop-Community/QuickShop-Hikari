/*
 * This file is a part of project QuickShop, the name is SimpleIntegrationManager.java
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

package org.maxgamer.quickshop.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.*;
import org.maxgamer.quickshop.integration.advancedregionmarket.AdvancedShopRegionMarketIntegration;
import org.maxgamer.quickshop.integration.fabledskyblock.FabledIntegration;
import org.maxgamer.quickshop.integration.factionsuuid.FactionsUUIDIntegration;
import org.maxgamer.quickshop.integration.griefprevention.GriefPreventionIntegration;
import org.maxgamer.quickshop.integration.iridiumskyblock.IridiumSkyblockIntegration;
import org.maxgamer.quickshop.integration.lands.LandsIntegration;
import org.maxgamer.quickshop.integration.plotsquared.PlotSquaredIntegrationV6;
import org.maxgamer.quickshop.integration.residence.ResidenceIntegration;
import org.maxgamer.quickshop.integration.superiorskyblock.SuperiorSkyblock2Integration;
import org.maxgamer.quickshop.integration.towny.TownyIntegration;
import org.maxgamer.quickshop.integration.worldguard.WorldGuardIntegration;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;
import org.maxgamer.quickshop.util.holder.Result;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class SimpleIntegrationManager extends QuickShopInstanceHolder implements IntegrationManager {
    private static final Map<String, Class<? extends IntegratedPlugin>> INTEGRATION_MAPPING = new HashMap<>(7);

    static {
        INTEGRATION_MAPPING.put("Factions", FactionsUUIDIntegration.class);
        INTEGRATION_MAPPING.put("GriefPrevention", GriefPreventionIntegration.class);
        INTEGRATION_MAPPING.put("Lands", LandsIntegration.class);
        INTEGRATION_MAPPING.put("PlotSquared", PlotSquaredIntegrationV6.class);
        INTEGRATION_MAPPING.put("Residence", ResidenceIntegration.class);
        INTEGRATION_MAPPING.put("Towny", TownyIntegration.class);
        INTEGRATION_MAPPING.put("WorldGuard", WorldGuardIntegration.class);
        INTEGRATION_MAPPING.put("FabledSkyblock", FabledIntegration.class);
        INTEGRATION_MAPPING.put("IridiumSkyblock", IridiumSkyblockIntegration.class);
        INTEGRATION_MAPPING.put("SuperiorSkyblock", SuperiorSkyblock2Integration.class);
        INTEGRATION_MAPPING.put("AdvancedRegionMarket", AdvancedShopRegionMarketIntegration.class);
    }

    private final Map<String, IntegratedPlugin> integrations = new HashMap<>(7);

    public SimpleIntegrationManager(QuickShop plugin) {
        super(plugin);
    }

    @Override
    public Map<String, IntegratedPlugin> getIntegrationMap() {
        return Collections.unmodifiableMap(integrations);
    }

    @Override
    public List<IntegratedPlugin> getIntegrations() {
        return Collections.unmodifiableList(new ArrayList<>(integrations.values()));
    }

    @Override
    public void searchAndRegisterPlugins() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (Map.Entry<String, Class<? extends IntegratedPlugin>> entry : INTEGRATION_MAPPING.entrySet()) {
            String pluginName = entry.getKey();
            if (pluginManager.isPluginEnabled(pluginName) && plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
                try {
                    register(entry.getValue());
                } catch (Exception exception) {
                    plugin.getLogger().log(Level.WARNING, "Failed to register integration " + entry.getKey() + "!", exception);
                }
            }
        }
    }

    /**
     * Register custom integrated module to QuickShop integration system
     *
     * @param integratedPlugin custom integrated module
     */
    @Override
    public void register(@NotNull IntegratedPlugin integratedPlugin) {
        if (!isIntegrationClass(integratedPlugin.getClass())) {
            throw new InvalidIntegratedPluginClassException("Invaild Integration module: " + integratedPlugin.getName());
        }
        if (!integrations.containsKey(integratedPlugin.getName())) {
            plugin.getLogger().info("Registering " + integratedPlugin.getName() + " integration");
            integrations.put(integratedPlugin.getName(), integratedPlugin);
        }
    }

    /**
     * Register custom integrated module to QuickShop integration system from a class
     *
     * @param integratedPluginClass custom integrated module class
     */
    @Override
    public void register(@NotNull Class<? extends IntegratedPlugin> integratedPluginClass) {
        IntegratedPlugin integratedPlugin;
        try {
            integratedPlugin = integratedPluginClass.getConstructor(plugin.getClass()).newInstance(plugin);
        } catch (NullPointerException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new InvalidIntegratedPluginClassException("Invalid Integration module class: " + integratedPluginClass, e);
        }
        register(integratedPlugin);
    }

    /**
     * Register custom integrated module to QuickShop integration system from a plugin name
     *
     * @param integratedPluginName custom integrated module name
     */
    @Override
    public void register(@NotNull String integratedPluginName) {
        Class<? extends IntegratedPlugin> integratedPluginClass = INTEGRATION_MAPPING.get(integratedPluginName);
        if (integratedPluginClass != null) {
            register(integratedPluginClass);
        } else {
            throw new InvalidIntegratedPluginClassException("Invalid Integration module name: " + integratedPluginName);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isIntegrationClass(@NotNull Class<?> clazz) {
        return clazz.getDeclaredAnnotation(IntegrationStage.class) != null;
    }

    /**
     * Unregister integrated plugin from Integration system
     *
     * @param integratedPluginName plugin name
     */
    @Override
    public void unregister(@NotNull String integratedPluginName) {
        IntegratedPlugin integratedPlugin = integrations.get(integratedPluginName);
        if (integratedPlugin != null) {
            unregister(integratedPlugin);
        }
    }

    /**
     * Unregister all integrated plugin from Integration system
     */
    @Override
    public void unregisterAll() {
        for (IntegratedPlugin integratedPlugin : new ArrayList<>(integrations.values())) {
            unregister(integratedPlugin);
        }
    }

    /**
     * Unregister integrated plugin from Integration system
     *
     * @param integratedPlugin plugin
     */
    @Override
    public void unregister(@NotNull IntegratedPlugin integratedPlugin) {
        if (!isIntegrationClass(integratedPlugin.getClass())) {
            throw new InvalidIntegratedPluginClassException();
        }

        plugin.getLogger().info("Unregistering " + integratedPlugin.getName() + " integration");
        Util.debugLog("Unregistering " + integratedPlugin.getName() + " integration");
        integrations.remove(integratedPlugin.getName());
    }

    @Override
    public boolean isRegistered(@NotNull String integrationName) {
        return false;
    }

    public void callIntegrationsLoad(@NotNull IntegrateStage stage) {
        integrations.values().forEach(
                integratedPlugin -> {
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).loadStage()
                            == stage) {
                        Util.debugLog("Calling for load " + integratedPlugin.getName());
                        integratedPlugin.load();
                    }
                });
    }

    public void callIntegrationsUnload(@NotNull IntegrateStage stage) {
        integrations.values().forEach(
                integratedPlugin -> {
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).unloadStage()
                            == stage) {
                        Util.debugLog("Calling for unload " + integratedPlugin.getName());
                        integratedPlugin.unload();
                    }
                });
    }

    public Result callIntegrationsCanCreate(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations.values()) {
            if (!plugin.canCreateShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

    public Result callIntegrationsCanTrade(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations.values()) {
            if (!plugin.canTradeShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

}
