/*
 * This file is a part of project QuickShop, the name is SimpleCompatibilityManager.java
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

package org.maxgamer.quickshop.util.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.compatibility.CompatibilityModule;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SimpleCompatibilityManager extends QuickShopInstanceHolder implements CompatibilityManager, Listener {
    private static final Map<String, Class<? extends CompatibilityModule>> MODULE_MAP = new HashMap<>(2);

    static {
        MODULE_MAP.put("NoCheatPlus", NCPCompatibilityModule.class);
        MODULE_MAP.put("Spartan", SpartanCompatibilityModule.class);
    }

    private final Map<String, CompatibilityModule> registeredModules = new HashMap<>(5);

    public SimpleCompatibilityManager(QuickShop plugin) {
        super(plugin);
    }

    public static Map<String, Class<? extends CompatibilityModule>> getModuleMapping() {
        return MODULE_MAP;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void scan(PluginEnableEvent event) {
        searchAndRegisterPlugins();
    }

    public void searchAndRegisterPlugins() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (Map.Entry<String, Class<? extends CompatibilityModule>> entry : MODULE_MAP.entrySet()) {
            String pluginName = entry.getKey();
            if (pluginManager.isPluginEnabled(pluginName) && !isRegistered(pluginName)) {
                register(entry.getValue());
            }
        }
    }

    @Override
    public boolean isRegistered(String pluginName) {
        return registeredModules.containsKey(pluginName);
    }

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    @Override
    public void toggleProtectionListeners(boolean status, @NotNull Player player) {
        for (CompatibilityModule module : this.registeredModules.values()) {
            try {
                module.toggle(player, status);
            } catch (Throwable e) {
                unregister(module);
                Util.debugLog("Unregistered module " + module.getName() + " for an error: " + e.getMessage());
            }
        }
    }

    @Override
    public void unregisterAll() {
        registeredModules.clear();
    }

    @Override
    public void register(@NotNull CompatibilityModule module) {
        if (!registeredModules.containsKey(module.getName())) {
            plugin.getLogger().info("Registering " + module.getName() + " Compatibility Module");
            registeredModules.put(module.getName(), module);
        }
    }

    public void register(@NotNull String moduleName) {
        Class<? extends CompatibilityModule> compatibilityModuleClass = MODULE_MAP.get(moduleName);
        if (compatibilityModuleClass != null) {
            register(compatibilityModuleClass);
        } else {
            throw new IllegalStateException("Invalid compatibility module name: " + moduleName);
        }
    }

    public void register(@NotNull Class<? extends CompatibilityModule> compatibilityModuleClass) {
        CompatibilityModule compatibilityModule;
        try {
            compatibilityModule = compatibilityModuleClass.getConstructor(plugin.getClass()).newInstance(plugin);
        } catch (NullPointerException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Invalid compatibility module class: " + compatibilityModuleClass, e);
        }
        register(compatibilityModule);
    }

    @Override
    public void unregister(@NotNull String moduleName) {
        plugin.getLogger().info("Unregistering " + moduleName + " compatibility module");
        registeredModules.remove(moduleName);
    }

    @Override
    public void unregister(@NotNull CompatibilityModule module) {
        plugin.getLogger().info("Unregistering " + module.getName() + " compatibility module");
        registeredModules.remove(module.getName());
    }
}
