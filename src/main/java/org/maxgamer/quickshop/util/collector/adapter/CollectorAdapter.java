/*
 * This file is a part of project QuickShop, the name is CollectorAdapter.java
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

package org.maxgamer.quickshop.util.collector.adapter;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.economy.EconomyCore;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.economy.Economy_Vault;
import org.maxgamer.quickshop.shop.ShopLoader;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.collector.CollectResolver;
import org.maxgamer.quickshop.util.collector.CollectType;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;

public class CollectorAdapter {
    @CollectResolver(field = CollectType.QUICKSHOP)
    public Map<?, ?> collectQuickShop(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        data.put("version", QuickShop.getVersion());
        data.put("fork", QuickShop.getFork());
        data.put("build_info", plugin.getBuildInfo());
        data.put("server_id", plugin.getServerUniqueID());
        data.put("openinv_hook", plugin.getOpenInvPlugin() == null ? "Disabled" : "Enabled");
        Map<String, String> economy = new HashMap<>();
        try {
            EconomyCore economyCore = plugin.getEconomy();
            //noinspection SwitchStatementWithTooFewBranches
            switch (AbstractEconomy.getNowUsing()) {
                case VAULT:
                    economy.put("core", "Vault");
                    economy.put("provider", ((Economy_Vault) economyCore).getProviderName());
                    break;
//                case RESERVE:
//                    economy.put("core", "Reserve");
//                    economy.put("provider", "No details");
//                    break;
                default:
                    economy.put("core", economyCore.getName());
                    economy.put("provider", economyCore.getPlugin().getName());
            }
        } catch (Exception e) {
            economy.put("core", "Unknown");
            economy.put("provider", "Error to getting data: " + e.getMessage());
        }
        data.put("economy", economy);
        return data;
    }

    @CollectResolver(field = CollectType.SYSTEM)
    public Map<?, ?> collectSystem(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        data.put("os_name", System.getProperty("os.name"));
        data.put("os_arch", System.getProperty("os.arch"));
        data.put("os_version", System.getProperty("os.version"));
        data.put("os_cores", Runtime.getRuntime().availableProcessors());
        data.put("vm_version", System.getProperty("java.version"));
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        data.put("vm_name", runtimeMxBean.getName());
        List<String> arguments = runtimeMxBean.getInputArguments();
        data.put("vm_arguments", Util.list2String(arguments));
        data.put("vm_classpath", runtimeMxBean.getClassPath());
        data.put("vm_uptime", runtimeMxBean.getUptime());
        return data;
    }

    @CollectResolver(field = CollectType.PLATFORM)
    public Map<?, ?> collectPlatform(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        data.put("name", plugin.getServer().getName());
        data.put("software", plugin.getServer().getVersion());
        data.put("version", plugin.getServer().getVersion());
        data.put("version_internal", ReflectFactory.getNMSVersion());
        data.put("version_data", plugin.getServer().getUnsafe().getDataVersion());
        data.put("online_mode", plugin.getServer().getOnlineMode());
        data.put("view_distance", plugin.getServer().getViewDistance());
        data.put("primary_thread_call", plugin.getServer().isPrimaryThread());
        data.put("online", plugin.getServer().getOnlinePlayers().size());
        data.put("total", plugin.getServer().getOfflinePlayers().length);
        return data;
    }

    @CollectResolver(field = CollectType.MODULES)
    public Map<?, ?> collectModules(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        data.put("item_matcher", plugin.getItemMatcher().getName() + "@" + plugin.getItemMatcher().getPlugin().getName());

        if (plugin.getEconomy() == null) {
            data.put("economy_core", "Not loaded@Unknown");
        } else {
            data.put("economy_core", plugin.getEconomy().getName() + "@" + plugin.getEconomy().getPlugin().getName());
        }
        data.put("database_core", plugin.getDatabaseManager().getDatabase().getName() + "@" + plugin.getDatabaseManager().getDatabase().getPlugin().getName());
        data.put("gamelanguage_processor", MsgUtil.gameLanguage.getName() + "@" + MsgUtil.gameLanguage.getPlugin().getName());
        return data;
    }

    @CollectResolver(field = CollectType.SERVICES)
    public Map<?, ?> collectServices(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        List<Map<?, ?>> content = new ArrayList<>();
        plugin.getServer().getServicesManager().getRegistrations(plugin).forEach(service -> {
            Map<String, Object> map = new HashMap<>();
            map.put("plugin", service.getPlugin().getName());
            map.put("priority", service.getPriority());
            map.put("provider", service.getProvider().getClass().getCanonicalName());
            map.put("service", service.getService().getCanonicalName());
            content.add(map);
        });
        data.put("enabled", content);
        return data;
    }

    @CollectResolver(field = CollectType.SHOPS_IN_WORLD)
    public Map<?, ?> collectShopsInWorld(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        plugin.getServer().getWorlds().forEach(world -> {
            Map<String, Object> perWorld = new HashMap<>();
            perWorld.put("uid", world.getUID());
            perWorld.put("players", world.getPlayers().size());
            perWorld.put("shops", Util.getShopsInWorld(world.getName()));
            perWorld.put("environment", world.getEnvironment());
            perWorld.put("entities", world.getEntities());
            perWorld.put("view_distance", world.getViewDistance());
            perWorld.put("items", world.getEntities().stream().filter(Item.class::isInstance).count()); //DISPLAY
            perWorld.put("armor_stands", world.getEntities().stream().filter(ArmorStand.class::isInstance).toArray().length); //DISPLAY
            perWorld.put("force_loaded_chunks", world.getForceLoadedChunks().size());
            perWorld.put("shops_in_force_loaded_chunks", plugin.getShopManager().getShopsInWorld(world).stream().filter(shop -> world.getForceLoadedChunks().contains(shop.getLocation().getChunk())).toArray().length);
            perWorld.put("max_height", world.getMaxHeight());
            perWorld.put("sea_level", world.getSeaLevel());
            data.put(world.getName(), perWorld);
        });
        return data;
    }

    @CollectResolver(field = CollectType.PLUGINS)
    public Map<?, ?> collectPlugins(@NotNull QuickShop plugin) {
        Map<Object, Object> data = new HashMap<>();
        Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).forEach(pl -> {
            Map<String, Object> perPlugin = new HashMap<>();
            perPlugin.put("data_folder", pl.getDataFolder());
            perPlugin.put("api_version", pl.getDescription().getAPIVersion());
            perPlugin.put("authors", Util.list2String(pl.getDescription().getAuthors()));
            perPlugin.put("depend", Util.list2String(pl.getDescription().getDepend()));
            perPlugin.put("soft_depend", Util.list2String(pl.getDescription().getSoftDepend()));
            perPlugin.put("is_addon", pl.getDescription().getDepend().contains("QuickShop") || pl.getDescription().getSoftDepend().contains("QuickShop"));
            perPlugin.put("description", pl.getDescription().getDescription());
            perPlugin.put("full_name", pl.getDescription().getFullName());
            perPlugin.put("load", pl.getDescription().getLoad().name());
            perPlugin.put("load_before", pl.getDescription().getLoadBefore());
            perPlugin.put("main", pl.getDescription().getMain());
            perPlugin.put("version", pl.getDescription().getVersion());
            perPlugin.put("website", pl.getDescription().getWebsite());
            data.put(pl.getName(), perPlugin);
        });
        return data;
    }

    @CollectResolver(field = CollectType.CONFIG)
    public Map<?, ?> collectConfig(@NotNull QuickShop plugin) {
        //TODO
        return null;
    }

    @CollectResolver(field = CollectType.SERVER_CONFIG)
    public Map<?, ?> collectServerConfig(@NotNull QuickShop plugin) {
        //TODO
        return null;
    }

    @CollectResolver(field = CollectType.LANGUAGE)
    public Map<?, ?> collectI18n(@NotNull QuickShop plugin) {
        //TODO
        return null;
    }

    @CollectResolver(field = CollectType.LOGS)
    public Map<?, ?> collectLogs(@NotNull QuickShop plugin) {
        Map<String, String> map = new HashMap<>();
        map.put("qs_debug", Util.list2String(Util.getDebugLogs()));
//        map.put("qs_shop", ); //TODO shop log
//        map.put("qs_sentry", ); //TODO sentry log
        return map;

    }

    @CollectResolver(field = CollectType.SHOPS)
    public Map<?, ?> collectShops(@NotNull QuickShop plugin) {
        Map<Object, Object> map = new HashMap<>();
        map.put("database", plugin
                .getShopLoader()
                .getOriginShopsInDatabase()
                .stream()
                .map(ShopLoader.ShopRawDatabaseInfo::toString)
                .toArray());
        map.put("memory", plugin.getShopManager().getAllShops().stream().map(Shop::toString).toArray());
        return map;

    }
}
