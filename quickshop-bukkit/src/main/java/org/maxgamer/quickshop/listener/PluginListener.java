///*
// * This file is a part of project QuickShop, the name is PluginListener.java
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
//package org.maxgamer.quickshop.listener;
//
//import com.ghostchu.simplereloadlib.ReloadResult;
//import com.ghostchu.simplereloadlib.ReloadStatus;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.server.PluginDisableEvent;
//import org.bukkit.event.server.PluginEnableEvent;
//import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
//
//import java.util.Set;
//
//public class PluginListener extends AbstractQSListener {
//
//    private static final Set<String> COMPATIBILITY_MODULE_LIST = SimpleCompatibilityManager.getModuleMapping().keySet();
//    private CompatibilityManager compatibilityManager;
//
//    public PluginListener(QuickShop plugin) {
//        super(plugin);
//        init();
//    }
//
//    private void init() {
//        compatibilityManager = plugin.getCompatibilityManager();
//    }
//
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPluginDisabled(PluginDisableEvent event) {
//        String pluginName = event.getPlugin().getName();
//        if (COMPATIBILITY_MODULE_LIST.contains(pluginName)) {
//            compatibilityManager.unregister(pluginName);
//        }
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPluginEnabled(PluginEnableEvent event) {
//        String pluginName = event.getPlugin().getName();
//        if (COMPATIBILITY_MODULE_LIST.contains(pluginName)) {
//            ((SimpleCompatibilityManager) compatibilityManager).register(pluginName);
//        }
//    }
//
//    /**
//     * Callback for reloading
//     *
//     * @return Reloading success
//     */
//    @Override
//    public ReloadResult reloadModule() {
//        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
//    }
//}
