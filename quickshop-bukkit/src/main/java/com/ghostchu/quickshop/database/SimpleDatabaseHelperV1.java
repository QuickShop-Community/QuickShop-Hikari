/*
 *  This file is a part of project QuickShop, the name is SimpleDatabaseHelper.java
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

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.enums.IndexType;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * A Util to execute all SQLs.
 */
@Deprecated
public class SimpleDatabaseHelperV1 {


    @NotNull
    private final SQLManager manager;

    @NotNull
    private final QuickShop plugin;

    @NotNull
    private final String prefix;

    public SimpleDatabaseHelperV1(@NotNull QuickShop plugin, @NotNull SQLManager manager, @NotNull String prefix) throws SQLException {
        this.plugin = plugin;
        this.manager = manager;
        this.prefix = prefix;
        checkTables();
        checkColumns();
    }

    public void checkTables() throws SQLException {
        if (!hasTable(prefix + "metadata")) {
            createMetadataTable();
        }
        if (!hasTable(prefix + "shops")) {
            createShopsTable();
        }
        if (!hasTable(prefix + "messages")) {
            createMessagesTable();
        }
        if (!hasTable(prefix + "logs")) {
            createLogsTable();
        }
        if (!hasTable(prefix + "external_cache")) {
            createExternalCacheTable();
        }
        if (!hasTable(prefix + "players")) {
            createPlayerTable();
        }
        if (!hasTable(prefix + "metrics")) {
            createMetricsTable();
        }
    }

    /**
     * Creates the database table 'shops'.
     */

    public void createShopsTable() {
        manager.createTable(prefix + "shops")
                .addColumn("owner", "MEDIUMTEXT NOT NULL")
                .addColumn("price", "DECIMAL(32,2) NOT NULL")
                .addColumn("itemConfig", "LONGTEXT NOT NULL")
                .addColumn("x", "INT(32) NOT NULL")
                .addColumn("y", "INT(32) NOT NULL")
                .addColumn("z", "INT(32) NOT NULL")
                .addColumn("world", "VARCHAR(255) NOT NULL")
                .addColumn("unlimited", "TINYINT NOT NULL")
                .addColumn("type", "INTEGER(8) NOT NULL")
                .addColumn("extra", "LONGTEXT NULL")
                .addColumn("currency", "TEXT NULL")
                .addColumn("disableDisplay", "TINYINT NULL DEFAULT 0")
                .addColumn("taxAccount", "VARCHAR(255) NULL")
                .addColumn("inventorySymbolLink", "TEXT NULL")
                .addColumn("inventoryWrapperName", "VARCHAR(255) NULL")
                .addColumn("name", "TEXT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "x", "y", "z", "world")
                .build().execute((i) -> {
                    return i;
                    // Do nothing
                }, ((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed while trying create the shop table! SQL: " + sqlAction.getSQLContent(), exception)));
    }

    /**
     * Creates the database table 'metrics'
     */
    public void createMetricsTable() {
        manager.createTable(prefix + "metrics")
                .addColumn("time", "BIGINT NOT NULL")
                .addColumn("x", "INT NOT NULL")
                .addColumn("y", "INT NOT NULL")
                .addColumn("z", "INT NOT NULL")
                .addColumn("world", "VARCHAR(255) NOT NULL")
                .addColumn("type", "VARCHAR(255) NOT NULL")
                .addColumn("total", "DECIMAL(32,2) NOT NULL")
                .addColumn("tax", "DECIMAL(32,2) NOT NULL")
                .addColumn("amount", "INT(32) NOT NULL")
                .addColumn("player", "VARCHAR(255) NOT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "time")
                .setIndex(IndexType.INDEX, "player_based", "time", "x", "y", "z", "world", "player")
                .setIndex(IndexType.INDEX, "type_based", "time", "x", "y", "z", "world", "type")
                .build()
                .execute(((exception, sqlAction) -> {
                    if (exception != null) {
                        plugin.getLogger().log(Level.WARNING, "Failed to create messages table! SQL:" + sqlAction.getSQLContent(), exception);
                    }
                }));
    }

    /**
     * Creates the database table 'messages'
     */
    public void createMessagesTable() {
        manager.createTable(prefix + "messages")
                .addColumn("owner", "VARCHAR(255) NOT NULL")
                .addColumn("message", "TEXT NOT NULL")
                .addColumn("time", "BIGINT(32) NOT NULL")
                .build()
                .execute(((exception, sqlAction) -> {
                    if (exception != null) {
                        plugin.getLogger().log(Level.WARNING, "Failed to create messages table! SQL:" + sqlAction.getSQLContent(), exception);
                    }
                }));
    }

    public void createLogsTable() {
        manager.createTable(prefix + "logs")
                .addColumn("time", "BIGINT(32) NOT NULL")
                .addColumn("classname", "TEXT NULL")
                .addColumn("data", "LONGTEXT NULL")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create logs table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    public void createExternalCacheTable() {
        manager.createTable(prefix + "external_cache")
                .addColumn("x", "INT(32) NOT NULL")
                .addColumn("y", "INT(32) NOT NULL")
                .addColumn("z", "INT(32) NOT NULL")
                .addColumn("world", "VARCHAR(255) NOT NULL")
                .addColumn("space", "INT NULL")
                .addColumn("stock", "INT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "x", "y", "z", "world")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create extrenal_cache table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    public void createMetadataTable() {
        manager.createTable(prefix + "metadata")
                .addColumn("key", "VARCHAR(255) NOT NULL")
                .addColumn("value", "LONGTEXT NOT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "key")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create metadata table! SQL:" + sqlAction.getSQLContent(), exception)));
        try {
            setDatabaseVersion(2);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update database version!", e);
        }
    }

    public void createPlayerTable() {
        manager.createTable(prefix + "players")
                .addColumn("uuid", "VARCHAR(255) NOT NULL")
                .addColumn("locale", "TEXT NOT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "uuid")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create players table! SQL:" + sqlAction.getSQLContent(), exception)));
    }



    /**
     * Verifies that all required columns exist.
     */
    public void checkColumns() throws SQLException {
        plugin.getLogger().info("Checking and updating database columns, it may take a while...");
        if (getDatabaseVersion() < 1) {
            // QuickShop v4/v5 upgrade
            // Call updater
            setDatabaseVersion(1);
        }
        if (getDatabaseVersion() == 1) {
            // QuickShop-Hikari 1.1.0.0
            try {
                manager.alterTable(prefix + "shops")
                        .addColumn("name", "TEXT NULL")
                        .execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.INFO, "Failed to add name column to shops table! SQL: " + e.getMessage());
            }
            plugin.getLogger().info("[DatabaseHelper] Migrated to 1.1.0.0 data structure, version 2");
            setDatabaseVersion(2);
        }
        if (getDatabaseVersion() == 2) {
            // QuickShop-Hikari 2.0.0.0
            try {
                manager.alterTable(prefix + "shops")
                        .addColumn("permission", "TEXT NULL")
                        .execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.INFO, "Failed to add name column to shops table! SQL: " + e.getMessage());
            }
            setDatabaseVersion(3);
            setDatabaseVersion(3);
        }
        plugin.getLogger().info("Finished!");
    }

    public void setDatabaseVersion(int version) throws SQLException {
        manager.createReplace(prefix + "metadata")
                .setColumnNames("key", "value")
                .setParams("database_version", version)
                .execute();
    }

    public int getDatabaseVersion() {
        try (SQLQuery query = manager.createQuery().inTable(prefix + "metadata")
                .addCondition("key", "database_version")
                .selectColumns("value")
                .build().execute(); ResultSet result = query.getResultSet()) {
            if (!result.next()) {
                return 0;
            }
            return Integer.parseInt(result.getString("value"));
        } catch (SQLException e) {
            Log.debug("Failed to getting database version! Err: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    public boolean hasTable(@NotNull String table) throws SQLException {
        Connection connection = manager.getConnection();
        boolean match = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    match = true;
                    break;
                }
            }
        } finally {
            connection.close();
        }
        return match;
    }

    public @NotNull SQLManager getManager() {
        return manager;
    }

    public @NotNull String getPrefix() {
        return prefix;
    }
}
