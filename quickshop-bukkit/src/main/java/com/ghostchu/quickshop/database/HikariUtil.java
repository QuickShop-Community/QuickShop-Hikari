/*
 *  This file is a part of project QuickShop, the name is HikariUtil.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.database;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.configuration.ConfigurationSection;

public class HikariUtil {
    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig(){
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        ConfigurationSection section = QuickShop.getInstance().getConfig().getConfigurationSection("database");
        if(section == null)
            throw new IllegalArgumentException("database section in configuration not found");
        section = section.getConfigurationSection("properties");
        if(section == null)
            throw new IllegalArgumentException("database.properties section in configuration not found");
        for (String key : section.getKeys(false)) {
            config.addDataSourceProperty(key, section.getString(key));
        }
        Util.debugLog("HikariConfig created with properties: " + config.getDataSourceProperties());
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
