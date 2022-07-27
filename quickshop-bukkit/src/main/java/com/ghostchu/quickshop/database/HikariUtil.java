package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.configuration.ConfigurationSection;

public class HikariUtil {
    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig() {
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        ConfigurationSection section = QuickShop.getInstance().getConfig().getConfigurationSection("database");
        if (section == null)
            throw new IllegalArgumentException("database section in configuration not found");
        section = section.getConfigurationSection("properties");
        if (section == null)
            throw new IllegalArgumentException("database.properties section in configuration not found");
        for (String key : section.getKeys(false)) {
            config.addDataSourceProperty(key, section.getString(key));
        }
        Log.debug("HikariCP Config created with properties: " + config.getDataSourceProperties());
//        config.addDataSourceProperty("connection-timeout", "60000");
//        config.addDataSourceProperty("validation-timeout", "3000");
//        config.addDataSourceProperty("idle-timeout", "60000");
//        config.addDataSourceProperty("login-timeout", "5");
//        config.addDataSourceProperty("maxLifeTime", "60000");
//        config.addDataSourceProperty("maximum-pool-size", QuickShop.getInstance().getConfig().getInt("database.max-pool-size"));
//        config.addDataSourceProperty("minimum-idle", "10");
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        config.addDataSourceProperty("useUnicode", "true");
//        config.addDataSourceProperty("characterEncoding", "utf8");
        return config;
    }
}
