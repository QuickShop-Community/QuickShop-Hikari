package com.ghostchu.quickshop.database;

public class HikariUtil {
    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig(){
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        config.addDataSourceProperty("connection-timeout", "60000");
        config.addDataSourceProperty("validation-timeout", "3000");
        config.addDataSourceProperty("idle-timeout", "60000");
        config.addDataSourceProperty("login-timeout", "5");
        config.addDataSourceProperty("maxLifeTime", "60000");
        config.addDataSourceProperty("maximum-pool-size", "8");
        config.addDataSourceProperty("minimum-idle", "10");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        return config;
    }
}
