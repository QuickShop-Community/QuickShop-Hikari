package com.ghostchu.quickshop.util.metric.collect;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.metric.MetricCollectEntry;
import com.ghostchu.quickshop.util.metric.MetricDataType;
import org.bstats.charts.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class BuiltInCollects {//Statistic
    private final QuickShop plugin;

    public BuiltInCollects(QuickShop plugin) {
        this.plugin = plugin;
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Addons or Compacts Discovered", description = "QuickShop collects the QuickShop's addons/compacts (including 3rd-party) list that installed on your server to discover new addons/compacts so we can contact authors when we have major API changes, or use for improve exists official addons/compacts who have most of users using.")
    public CustomChart researchAddonsCompacts() {
        return new AdvancedPie("research_addons_or_compacts_discovered", () -> {
            String myName = plugin.getJavaPlugin().getDescription().getName();
            Map<String, Integer> data = new HashMap<>();
            for (Plugin discoverPlugin : Bukkit.getPluginManager().getPlugins()) {
                PluginDescriptionFile descriptionFile = discoverPlugin.getDescription();
                if (descriptionFile.getDepend().contains(myName) || descriptionFile.getSoftDepend().contains(myName)) {
                    data.put(descriptionFile.getName(), 1);
                }
            }
            if (data.isEmpty()) {
                data.put("None", 1);
            }
            return data;
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Item Stacking Shop", description = "We collect this for determine if we should push Item Stacking Shop to a feature that default enabled.")
    public CustomChart researchItemStackingShop() {
        return new SimplePie("research_item_stacking_shop", () -> String.valueOf(plugin.isAllowStack()));
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Protection Listener Blacklist", description = "We collect this for determine if we should add common listener blacklist entry to default configuration.")
    public CustomChart researchProtectionListenerBlacklist() {
        return new AdvancedPie("research_protection_checker_blacklist", () -> {
            Map<String, Integer> data = new HashMap<>();
            plugin.getConfig().getStringList("shop.protection-checking-listener-blacklist").forEach(s -> data.put(s, 1));
            return data;
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Command Alias", description = "We collect this for determine if we should add/remove alias to default configuration.")
    public CustomChart researchCommandAlias() {
        return new AdvancedPie("research_command_alias", () -> {
            Map<String, Integer> data = new HashMap<>();
            plugin.getConfig().getStringList("custom-commands").forEach(s -> data.put(s, 1));
            return data;
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Types", description = "We collect this so we can know the percent of different database types users.")
    public CustomChart statisticDatabaseTypes() {
        return new SimplePie("statistic_database_types", () -> plugin.getDatabaseDriverType().name());
    }


    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Product", description = "We collect this so we can know the database vendor that users using, and provide dedicated driver if possible.")
    public CustomChart statisticDatabaseProject() {
        return new SimplePie("statistic_database_product", () -> {
            try (Connection connection = plugin.getSqlManager().getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                return metaData.getDatabaseProductName();
            } catch (Throwable throwable) {
                Log.debug("Populate statistic database version failed: " + throwable.getClass().getName() + ": " + throwable.getMessage());
                return "Error";
            }
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Version", description = "We collect this so we can know the database versions that users using, and provide/update dedicated driver or drop the support for too old database versions.")
    public CustomChart statisticDatabaseVersion() {
        return new SimplePie("statistic_database_version", () -> {
            try (Connection connection = plugin.getSqlManager().getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                return metaData.getDatabaseProductName() + "@" + metaData.getDatabaseProductVersion();
            } catch (Throwable throwable) {
                Log.debug("Populate statistic database version failed: " + throwable.getClass().getName() + ": " + throwable.getMessage());
                return "Error";
            }
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Economy Types", description = "We collect this so we can know the percent of different economy types users.")
    public CustomChart statisticEconomyTypes() {
        return new SimplePie("statistic_economy_types", () -> plugin.getEconomy().getName());
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - ItemMatcher", description = "We collect this so we can know the item matcher that users using, and improve it.")
    public CustomChart statisticItemMatcher() {
        return new SimplePie("statistic_item_matcher", () -> plugin.getItemMatcher().getName());
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - DisplayImpl", description = "We collect this so we can know the which one item display impl most using, and improve it.")
    public CustomChart statisticDisplayImpl() {
        return new SimplePie("statistic_displayimpl", () -> AbstractDisplayItem.getNowUsing().name());
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All shops hosting across all servers", description = "How many shops we can power across all servers? This research will used for performance tweak for components like shop managing/looking up/caching size etc.")
    public CustomChart statisticAllShops() {
        return new SingleLineChart("statistic_all_shops_hosting_across_all_servers", () -> plugin.getShopManager().getAllShops().size());
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Background Debug Logger", description = "We collect this so we can know the which one item display impl most using, and improve it.")
    public CustomChart statisticBackgroundDebugLogger() {
        return new SimplePie("statistic_background_debug_logger", () -> {
            if (plugin.getConfig().getBoolean("debug.disable-debuglogger")) {
                return "Disabled";
            } else {
                return "Enabled";
            }
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - ProtocolLib Version", description = "We collect this so we can know the which one ProtocolLib is popular. ProtocolLib sometimes releases destructive updates, so we collect this metric to know the distribution of ProtocolLib versions among users and remove unused ProtocolLib workaround code to improve code maintainability and program performance.")
    public CustomChart researchProtocolLibVersion() {
        return new SimplePie("research_protocollib_version", () -> {
            Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
            if (protocolLib == null) {
                return "Not Installed";
            }
            return protocolLib.getDescription().getVersion();
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Server Software Build Version", description = "Spigot and Paper always release updates during their version support cycles. Counting the server-side software versions used by users lets us know which builds are popular. And it allows us to be more aggressive with newly added APIs, This can improve code maintainability, stability and program performance.")
    public CustomChart statisticServerSoftwareBuildVersion() {
        return new DrilldownPie("statistic_server_software_build_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(Bukkit.getServer().getVersion(), 1);
            map.put(Bukkit.getServer().getName(), entry);
            return map;
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Publisher", description = "We count the name of the publisher (in BuildInfo) so that we know if someone else is recompiling our plugin without changing the fork name. if you are a QuickShop-Hikari fork developer, please change the return value of your getFork() to something else in order to separate it from the stats. This value is usually fixed to Ghost-chu@Hikari.")
    public CustomChart statisticPublisher() {
        return new SimplePie("statistic_publisher", () -> plugin.getBuildInfo().getGitInfo().getCommitUsername() + "@" + plugin.getFork());
    }

    @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Geyser", description = "We've released the Suspension Closure expansion for Geyser, but we're ultimately undecided about a Geyser-specific update. The data collected from this study allows us to analyze the QuickShop-Hikari user base to check if Geyser or Floodgate is installed, and with the percentage of users who have the statistics, we will decide whether to add support for Geyser GUIs and the like. We also welcome your feedback on our Discord server.")
    public CustomChart researchGeyser() {
        return new SimplePie("research_geyser", () -> {
            StringJoiner joiner = new StringJoiner("+");
            joiner.setEmptyValue("Not detected");
            Plugin geyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
            if (geyser != null) {
                joiner.add("Geyser-Spigot");
            }
            Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
            if (floodgate != null) {
                joiner.add("Floodgate");
            }
            return joiner.toString();
        });
    }
}
