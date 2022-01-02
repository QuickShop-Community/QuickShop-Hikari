/*
 * This file is a part of project QuickShop, the name is Paste.java
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

package org.maxgamer.quickshop.util.paste;

import com.google.common.cache.CacheStats;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.database.WarpedResultSet;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.economy.EconomyCore;
import org.maxgamer.quickshop.economy.Economy_Vault;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A util to generate a paste report and upload it to EngineHub/Ubuntu Paste
 */
@AllArgsConstructor
public class Paste {
    private final QuickShop plugin;

    /**
     * Create a server infomation paste
     *
     * @return The paste result content.
     */
    @SneakyThrows
    public @NotNull String genNewPaste() {
        StringBuilder finalReport = new StringBuilder();
        finalReport.append("###############################\n");
        finalReport.append("QuickShop-").append(QuickShop.getFork()).append(" Paste Result\n");
        finalReport.append("###############################\n");
        finalReport.append("\n");
        if (plugin.getServer().getPluginManager().getPlugin("ConsoleSpamFix") != null) {
            finalReport.append("Warning: ConsoleSpamFix installed! Please disable it before reporting any errors!").append("\n");
        }
        finalReport.append("\n");
        finalReport.append("================================================\n");
        finalReport.append("QuickShop:\n");
        finalReport.append("\tVersion: ").append(QuickShop.getVersion()).append("\n");
        finalReport.append("\tFork: ").append(QuickShop.getFork()).append("\n");
        finalReport.append("\tBuild Number: ").append(plugin.getBuildInfo().getBuildId()).append("\n");
        finalReport.append("\tBuild Branch: ").append(plugin.getBuildInfo().getGitBranch()).append("\n");
        finalReport.append("\tBuild Commit: ").append(plugin.getBuildInfo().getGitCommit()).append("\n");
        finalReport.append("\tBuild URL: ").append(plugin.getBuildInfo().getBuildUrl()).append("\n");
        finalReport.append("\tBuild Tag: ").append(plugin.getBuildInfo().getBuildTag()).append("\n");
        finalReport.append("\tChat System: ").append("Hardcoded Adventure").append("\n");
        finalReport.append("\tServer ID: ").append(plugin.getServerUniqueID()).append("\n");
        finalReport
                .append("\tOpenInv Hook: ")
                .append(plugin.getOpenInvPlugin() == null ? "Disabled" : "Enabled")
                .append("\n");
        finalReport.append("\tEconomy System: ");
        try {
            EconomyCore economyCore = plugin.getEconomy();
            //noinspection SwitchStatementWithTooFewBranches
            switch (AbstractEconomy.getNowUsing()) {
                case VAULT:
                    finalReport
                            .append("Vault")
                            .append("%")
                            .append(((Economy_Vault) economyCore).getProviderName());
                    break;
//                case RESERVE:
//                    finalReport.append("Reserve").append("%").append("No details");
//                    break;
                default:
                    finalReport.append("Unknown").append("%").append("Unknown error");
                    break;
            }
        } catch (Exception e) {
            finalReport.append("Unknown").append("%").append("Unknown error");
        }

        finalReport.append("\n");
        finalReport.append("================================================\n");
        finalReport.append("System:\n");
        finalReport.append("\tOS: ").append(System.getProperty("os.name")).append("\n");
        finalReport.append("\tArch: ").append(System.getProperty("os.arch")).append("\n");
        finalReport.append("\tVersion: ").append(System.getProperty("os.version")).append("\n");
        finalReport.append("\tCores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Server:\n");
        finalReport.append("\tName: ").append(plugin.getServer().getName()).append("\n");
        finalReport.append("\tServer Name: ").append(plugin.getServer().getName()).append("\n");
        finalReport.append("\tBuild: ").append(plugin.getServer().getVersion()).append("\n");
        finalReport.append("\tNMSV: ").append(ReflectFactory.getNMSVersion()).append("\n");
        //noinspection deprecation
        finalReport.append("\tData Version: ").append(plugin.getServer().getUnsafe().getDataVersion()).append("\n");
        if (plugin.getEnvironmentChecker().isFabricBasedServer() || plugin.getEnvironmentChecker().isForgeBasedServer()) {
            if (plugin.getEnvironmentChecker().isForgeBasedServer()) {
                finalReport.append("\tModded Server: Forge (No support offer on this platform)\n");
            }
            if (plugin.getEnvironmentChecker().isFabricBasedServer()) {
                finalReport.append("\tModded Server: Fabric (No support offer on this platform)\n");
            }
        }
        finalReport.append("\tJava: ").append(System.getProperty("java.version")).append("\n");
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        finalReport.append("\tInput Args: ").append(Util.list2String(arguments)).append("\n");
        finalReport.append("\tVM Name: ").append(runtimeMxBean.getVmName()).append("\n");
        Map<String, String> sys = runtimeMxBean.getSystemProperties();
        List<String> sysData = new ArrayList<>();
        sys.keySet().forEach(key -> sysData.add(key + "=" + sys.get(key)));
        finalReport.append("\tSystem Properties: ").append(Util.list2String(sysData)).append("\n");
        finalReport
                .append("\tPlayers: ")
                .append(plugin.getServer().getOnlinePlayers().size())
                .append("/")
                .append(plugin.getServer().getMaxPlayers())
                .append("\n");
        finalReport.append("\tOnlineMode: ").append(plugin.getServer().getOnlineMode()).append("\n");
        finalReport.append("\tBukkitVersion: ").append(plugin.getServer().getVersion()).append("\n");
        finalReport.append("\tWorldContainer: ").append(plugin.getServer().getWorldContainer()).append("\n");
        List<String> modules = new ArrayList<>();
        plugin.getIntegrationHelper().getIntegrations().forEach(m -> modules.add(m.getName()));
        finalReport.append("\tLoaded Integrations: ").append(Util.list2String(modules)).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Replaceable Modules Status:\n");
        finalReport.append("\tItemMatcher: ").append(plugin.getItemMatcher().getName()).append("@").append(plugin.getItemMatcher().getPlugin().getName()).append("\n");
        if (plugin.getEconomy() == null) {
            finalReport.append("\tEconomyCore: ").append("Not loaded").append("@").append("Unknown").append("\n");
        } else {
            finalReport.append("\tEconomyCore: ").append(plugin.getEconomy().getName()).append("@").append(plugin.getEconomy().getPlugin().getName()).append("\n");
        }
        finalReport.append("\tDatabaseCore: ").append(plugin.getDatabaseManager().getDatabase().getName()).append("@").append(plugin.getDatabaseManager().getDatabase().getPlugin().getName()).append("\n");
        finalReport.append("\tGameLanguage Processor: ").append(MsgUtil.gameLanguage.getName()).append("@").append(MsgUtil.gameLanguage.getPlugin().getName()).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Active shops on the server:\n");
        finalReport.append("\tTotal: ").append(plugin.getShopManager().getLoadedShops().size()).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Worlds:\n");
        finalReport.append("\tTotal: ").append(plugin.getServer().getWorlds().size()).append("\n");
        for (World world : plugin.getServer().getWorlds()) {
            finalReport.append("\t*********************************\n");
            finalReport.append("\t\tName: ").append(world.getName()).append("\n");
            finalReport.append("\t\tEnvironment: ").append(world.getEnvironment().name()).append("\n");
            //finalReport.append("\t\tLoaded Chunks: ").append(world.getLoadedChunks().length).append("\n");
            finalReport.append("\t\tPlayer In World: ").append(world.getPlayers().size()).append("\n");
            finalReport
                    .append("\t\tShops In World: ")
                    .append(Util.getShopsInWorld(world.getName()))
                    .append("\n");
        }
        finalReport.append("\t*********************************\n"); // Add a line after last world


        finalReport.append("================================================\n");
        finalReport.append("Plugins:\n");
        finalReport
                .append("\tTotal: ")
                .append(plugin.getServer().getPluginManager().getPlugins().length)
                .append("\n");
        for (Plugin bplugin : plugin.getServer().getPluginManager().getPlugins()) {
            finalReport
                    .append("\t")
                    .append(bplugin.getName())
                    .append(" @ ")
                    .append(bplugin.isEnabled() ? "Enabled" : "Disabled")
                    .append(" # ")
                    .append(bplugin.getDescription().getVersion())
                    .append(" # ")
                    .append(bplugin.getDescription().getAPIVersion());
            if (bplugin.getDescription().getDepend().contains(plugin.getName()) || bplugin.getDescription().getSoftDepend().contains(plugin.getName())) {
                finalReport.append(" # [Addon/Compatible Module]");
            }
            finalReport.append(" # ");
            String className;
            String packageName;
            Class<?> pluginClass = bplugin.getClass();
            Package pluginPackage = pluginClass.getPackage();
            className = pluginClass.getName();
            if (pluginPackage == null) {
                packageName = "[Default Package]";
            } else {
                packageName = pluginPackage.getName();
            }
            if (className.startsWith(packageName)) {
                finalReport.append(className);
            } else {
                finalReport.append(packageName).append(".").append(className);
            }
            finalReport.append("\n");
        }


        finalReport.append("================================================\n");
        finalReport.append("Performance:\n");
        finalReport.append("\tCache:\n");
        finalReport.append("\t\tCache      Enabled: ").append(plugin.getShopCache() != null).append("\n");
        if (plugin.getShopCache() != null) {
            CacheStats stats = plugin.getShopCache().getStats();
            finalReport.append("\t\t--------------------------").append("\n");
            finalReport.append("\t\tAvg  Load  Penalty: ").append(stats.averageLoadPenalty()).append("\n");
            finalReport.append("\t\tHit           Rate: ").append(stats.hitRate()).append("\n");
            finalReport.append("\t\tMiss          Rate: ").append(stats.missRate()).append("\n");
            finalReport.append("\t\t--------------------------").append("\n");
            finalReport.append("\t\tHit          Count: ").append(stats.hitCount()).append("\n");
            finalReport.append("\t\tMiss         Count: ").append(stats.missCount()).append("\n");
            finalReport.append("\t\tLoad         Count: ").append(stats.loadCount()).append("\n");
            finalReport.append("\t\tLoad Success Count: ").append(stats.loadSuccessCount()).append("\n");
            finalReport.append("\t\t--------------------------").append("\n");
            finalReport.append("\t\tEviction     Count: ").append(stats.evictionCount()).append("\n");
            finalReport.append("\t\tEviction     Count: ").append(stats.evictionCount()).append("\n");
            finalReport.append("\t\t--------------------------").append("\n");
            finalReport.append("\t\tRequest      Count: ").append(stats.requestCount()).append("\n");
            finalReport.append("\t\tTotal Loading Time: ").append(stats.totalLoadTime()).append("\n");
        }

        finalReport.append("================================================\n");
        finalReport.append("Configurations:\n");
        try {
            finalReport.append("\t*********************************\n");
            finalReport.append("\tconfig.yml:\n");
            finalReport.append("\t\t\n");
            String config = new String(
                    Objects.requireNonNull(
                            Util.inputStream2ByteArray(plugin.getDataFolder() + "/config.yml")),
                    StandardCharsets.UTF_8);
            // Process the data to protect passwords.
            try {
                ConfigurationSection configurationSection =
                        plugin.getConfig().getConfigurationSection("database");
                config =
                        config.replaceAll(
                                Objects.requireNonNull(
                                        Objects.requireNonNull(configurationSection).getString("user")),
                                "[PROTECTED]");
                config =
                        config.replace(
                                Objects.requireNonNull(configurationSection.getString("password")), "[PROTECTED]");
                config =
                        config.replace(
                                Objects.requireNonNull(configurationSection.getString("host")), "[PROTECTED]");
                config =
                        config.replace(
                                Objects.requireNonNull(configurationSection.getString("port")), "[PROTECTED]");
                config =
                        config.replace(
                                Objects.requireNonNull(configurationSection.getString("database")), "[PROTECTED]");
            } catch (Exception tg) {
                // Ignore
            }
            finalReport.append(config)
                    .append("\n");
//            finalReport.append("\t*********************************\n");
//            finalReport.append("\tmessages.json:\n");
//            finalReport
//                    .append("\t\t\n")
//                    .append(
//                            new String(
//                                    Objects.requireNonNull(
//                                            Util.inputStream2ByteArray(plugin.getDataFolder() + "/messages.json")),
//                                    StandardCharsets.UTF_8))
//                    .append("\n");

            finalReport.append("\t*********************************\n");
            finalReport.append("\titemi18n.yml:\n");
            finalReport
                    .append("\t\t\n")
                    .append(
                            new String(
                                    Objects.requireNonNull(
                                            Util.inputStream2ByteArray(
                                                    new File(plugin.getDataFolder(), "itemi18n.yml").getPath())),
                                    StandardCharsets.UTF_8))
                    .append("\n");

            finalReport.append("\t*********************************\n");
            finalReport.append("\tenchi18n.yml:\n");
            finalReport
                    .append("\t\t\n")
                    .append(
                            new String(
                                    Objects.requireNonNull(
                                            Util.inputStream2ByteArray(
                                                    new File(plugin.getDataFolder(), "enchi18n.yml").getPath())),
                                    StandardCharsets.UTF_8))
                    .append("\n");

            finalReport.append("\t*********************************\n");
            finalReport.append("\tpotioni18n.yml:\n");
            finalReport
                    .append("\t\t\n")
                    .append(
                            new String(
                                    Objects.requireNonNull(
                                            Util.inputStream2ByteArray(
                                                    new File(plugin.getDataFolder(), "potioni18n.yml").getPath())),
                                    StandardCharsets.UTF_8))
                    .append("\n");

            finalReport.append("\t*********************************\n");
            finalReport.append("\tInternal Debug Log:\n");
            finalReport
                    .append("\t\t\n")
                    .append(Util.list2String(Util.getDebugLogs()).replaceAll(",", "\n"))
                    .append("\n");
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tbukkit.yml:\n");
                finalReport
                        .append("\t\t\n")
                        .append(
                                new String(
                                        Objects.requireNonNull(
                                                Util.inputStream2ByteArray(
                                                        new File(new File("."), "bukkit.yml").getPath())),
                                        StandardCharsets.UTF_8))
                        .append("\n");

            } catch (Exception th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tbukkit.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");

            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tspigot.yml:\n");
                finalReport
                        .append("\t\t\n")
                        .append(
                                new String(
                                        Objects.requireNonNull(
                                                Util.inputStream2ByteArray(
                                                        new File(new File("."), "spigot.yml").getPath())),
                                        StandardCharsets.UTF_8))
                        .append("\n");
            } catch (Exception th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tspigot.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpaper.yml:\n");
                finalReport
                        .append("\t\t\n")
                        .append(
                                new String(
                                        Objects.requireNonNull(
                                                Util.inputStream2ByteArray(new File(new File("."), "paper.yml").getPath())),
                                        StandardCharsets.UTF_8).replaceAll("secret:.*", "secret: [PROTECTED]"))
                        .append("\n");
            } catch (Exception th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpaper.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
            }
//            try {
//                finalReport.append("\t*********************************\n");
//                finalReport.append("\ttuinity.yml:\n");
//                finalReport
//                        .append("\t\t\n")
//                        .append(
//                                new String(
//                                        Objects.requireNonNull(
//                                                Util.inputStream2ByteArray(new File(new File("."), "tuinity.yml").getPath())),
//                                        StandardCharsets.UTF_8))
//                        .append("\n");
//            } catch (Exception th) {
//                finalReport.append("\t*********************************\n");
//                finalReport.append("\ttuinity.yml:\n");
//                finalReport.append("\t\t\n").append("Read failed.").append("\n");
//            }
            try {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpurpur.yml:\n");
                finalReport
                        .append("\t\t\n")
                        .append(
                                new String(
                                        Objects.requireNonNull(
                                                Util.inputStream2ByteArray(new File(new File("."), "purpur.yml").getPath())),
                                        StandardCharsets.UTF_8))
                        .append("\n");
            } catch (Exception th) {
                finalReport.append("\t*********************************\n");
                finalReport.append("\tpurpur.yml:\n");
                finalReport.append("\t\t\n").append("Read failed.").append("\n");
            }
        } catch (Exception ignored) {
            finalReport.append("\tFailed to get data\n");
        }
        try {
            finalReport.append("\t*********************************\n");
            finalReport.append("\tairplane.yml:\n");
            finalReport
                    .append("\t\t\n")
                    .append(
                            new String(
                                    Objects.requireNonNull(
                                            Util.inputStream2ByteArray(new File(new File("."), "airplane.yml").getPath())),
                                    StandardCharsets.UTF_8))
                    .append("\n");
        } catch (Exception th) {
            finalReport.append("\t*********************************\n");
            finalReport.append("\tairplane.yml:\n");
            finalReport.append("\t\t\n").append("Read failed.").append("\n");
        }
        finalReport.append("================================================\n");
        finalReport.append("Shops in DB:\n");
        plugin
                .getShopLoader()
                .getOriginShopsInDatabase()
                .forEach(
                        (shopDatabaseInfoOrigin ->
                                finalReport.append("\t").append(shopDatabaseInfoOrigin).append("\n")));
        finalReport
                .append("Total: ")
                .append(plugin.getShopLoader().getOriginShopsInDatabase().size())
                .append("\n");
        finalReport.append("================================================\n");
        int totalDB = 0;

        try (WarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllShops()) {
            while (warpRS.getResultSet().next()) {
                totalDB++;
            }
        }

        finalReport.append("Shops in DB(RealTime): ").append(totalDB).append("\n");
        finalReport.append("================================================\n");
        finalReport.append("Shops in Mem:\n");
//        plugin
//                .getShopManager()
//                .getAllShops()
//                .forEach((shop -> finalReport.append(shop).append("\n")));
        finalReport
                .append("Total: ")
                .append(plugin.getShopManager().getAllShops().size())
                .append("\n");
        finalReport.append("================================================\n");


        return finalReport.toString();
    }

    @Nullable
    public String paste(@NotNull String content) {
        PasteInterface paster;
        try {
            paster = new HelpChatPastebinPaster();
            return paster.pasteTheText(content);
        } catch (Exception ex) {
            Util.debugLog(ex.getMessage());
        }
        try {
            // Lucko Pastebin
            paster = new LuckoPastebinPaster();
            return paster.pasteTheText(content);
        } catch (Exception ex) {
            Util.debugLog(ex.getMessage());
        }
        try {
            // Pastebin
            paster = new PastebinPaster();
            return paster.pasteTheText(content);
        } catch (Exception ex) {
            Util.debugLog(ex.getMessage());
        }
        try {
            // Ubuntu Pastebin
            paster = new UbuntuPaster();
            return paster.pasteTheText(content);
        } catch (Exception ex) {
            Util.debugLog(ex.getMessage());
        }
        return null;
    }


    @Nullable
    public String paste(@NotNull String content, PasteType type) {
        PasteInterface paster;
        switch (type) {
            case PASTEBIN:
                try {
                    // EngineHub Pastebin
                    paster = new PastebinPaster();
                    return paster.pasteTheText(content);
                } catch (Exception ignore) {
                }
                break;
            case HELPCHAT:
                try {
                    // Ubuntu Pastebin
                    paster = new HelpChatPastebinPaster();
                    return paster.pasteTheText(content);
                } catch (Exception ignore) {
                }
                break;
            case LUCKO:
                try {
                    // Lucko Pastebin
                    paster = new LuckoPastebinPaster();
                    return paster.pasteTheText(content);
                } catch (Exception ignore) {
                }
                break;
            default:
                try {
                    // Ubuntu Pastebin
                    paster = new UbuntuPaster();
                    return paster.pasteTheText(content);
                } catch (Exception ignore) {
                }
                break;
        }
        return null;
    }

    public enum PasteType {
        LUCKO,
        PASTEBIN,
        UBUNTU,
        HELPCHAT
    }
}
