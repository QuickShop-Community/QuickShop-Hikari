package com.ghostchu.quickshop.util.metric.collect;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.metric.MetricCollectEntry;
import com.ghostchu.quickshop.util.metric.MetricDataType;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.HashMap;
import java.util.Map;

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
            plugin.getConfig().getStringList("shop.protection-checking-listener-blacklist").forEach(s -> {
                data.put(s, 1);
            });
            return data;
        });
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Types", description = "We collect this so we can know the percent of different database types users.")
    public CustomChart statisticDatabaseTypes() {
        return new SimplePie("statistic_database_types", () -> plugin.getDatabaseDriverType().name());
    }

    @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Economy Types", description = "We collect this so we can know the percent of different economy types users.")
    public CustomChart statisticEconomyTypes() {
        return new SimplePie("statistic_economy_types", () -> plugin.getEconomy().getName());
    }
}
