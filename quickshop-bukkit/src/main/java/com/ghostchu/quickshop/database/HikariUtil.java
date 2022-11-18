package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.configuration.ConfigurationSection;

public class HikariUtil {
    private HikariUtil() {
    }

    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig() {
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        ConfigurationSection section = QuickShop.getInstance().getConfig().getConfigurationSection("database");
        if (section == null) {
            throw new IllegalArgumentException("database section in configuration not found");
        }
        section = section.getConfigurationSection("properties");
        if (section == null) {
            throw new IllegalArgumentException("database.properties section in configuration not found");
        }
        for (String key : section.getKeys(false)) {
            config.addDataSourceProperty(key, section.getString(key));
        }
        Log.debug("HikariCP Config created with properties: " + config.getDataSourceProperties());
        return config;
    }
}
