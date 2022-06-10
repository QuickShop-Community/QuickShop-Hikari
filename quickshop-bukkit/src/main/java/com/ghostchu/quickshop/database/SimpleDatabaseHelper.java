///*
// *  This file is a part of project QuickShop, the name is SimpleDatabaseHelper.java
// *  Copyright (C) Ghost_chu and contributors
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
//package com.ghostchu.quickshop.database;
//
//import cc.carm.lib.easysql.api.SQLManager;
//import cc.carm.lib.easysql.api.SQLQuery;
//import cc.carm.lib.easysql.api.enums.IndexType;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.database.DatabaseHelper;
//import com.ghostchu.quickshop.api.database.ShopMetricRecord;
//import com.ghostchu.quickshop.api.shop.Shop;
//import com.ghostchu.quickshop.util.JsonUtil;
//import com.ghostchu.quickshop.util.Util;
//import com.ghostchu.quickshop.util.logger.Log;
//import org.bukkit.Location;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.sql.*;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.function.Consumer;
//import java.util.logging.Level;
//
///**
// * A Util to execute all SQLs.
// */
//public class SimpleDatabaseHelper implements DatabaseHelper {
//
//
//    @NotNull
//    private final SQLManager manager;
//
//    @NotNull
//    private final QuickShop plugin;
//
//    @NotNull
//    private final String prefix;
//
//    public SimpleDatabaseHelper(@NotNull QuickShop plugin, @NotNull SQLManager manager, @NotNull String prefix) throws SQLException {
//        this.plugin = plugin;
//        this.manager = manager;
//        this.prefix = prefix;
//        checkTables();
//        checkColumns();
//    }
//
//    public void checkTables() throws SQLException {
//        if (!hasTable(prefix + "metadata")) {
//            createMetadataTable();
//        }
//        if (!hasTable(prefix + "shops")) {
//            createShopsTable();
//        }
//        if (!hasTable(prefix + "messages")) {
//            createMessagesTable();
//        }
//        if (!hasTable(prefix + "logs")) {
//            createLogsTable();
//        }
//        if (!hasTable(prefix + "external_cache")) {
//            createExternalCacheTable();
//        }
//        if (!hasTable(prefix + "players")) {
//            createPlayerTable();
//        }
//        if (!hasTable(prefix + "metrics")) {
//            createMetricsTable();
//        }
//    }
//
//    /**
//     * Creates the database table 'shops'.
//     */
//
//    public void createShopsTable() {
//        manager.createTable(prefix + "shops")
//                .addColumn("owner", "MEDIUMTEXT NOT NULL")
//                .addColumn("price", "DECIMAL(32,2) NOT NULL")
//                .addColumn("itemConfig", "LONGTEXT NOT NULL")
//                .addColumn("x", "INT(32) NOT NULL")
//                .addColumn("y", "INT(32) NOT NULL")
//                .addColumn("z", "INT(32) NOT NULL")
//                .addColumn("world", "VARCHAR(255) NOT NULL")
//                .addColumn("unlimited", "TINYINT NOT NULL")
//                .addColumn("type", "INTEGER(8) NOT NULL")
//                .addColumn("extra", "LONGTEXT NULL")
//                .addColumn("currency", "TEXT NULL")
//                .addColumn("disableDisplay", "TINYINT NULL DEFAULT 0")
//                .addColumn("taxAccount", "VARCHAR(255) NULL")
//                .addColumn("inventorySymbolLink", "TEXT NULL")
//                .addColumn("inventoryWrapperName", "VARCHAR(255) NULL")
//                .addColumn("name", "TEXT NULL")
//                .setIndex(IndexType.PRIMARY_KEY, null, "x", "y", "z", "world")
//                .build().execute((i) -> {
//                    return i;
//                    // Do nothing
//                }, ((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed while trying create the shop table! SQL: " + sqlAction.getSQLContent(), exception)));
//    }
//
//    /**
//     * Creates the database table 'metrics'
//     */
//    public void createMetricsTable() {
//        manager.createTable(prefix + "metrics")
//                .addColumn("time", "BIGINT NOT NULL")
//                .addColumn("x", "INT NOT NULL")
//                .addColumn("y", "INT NOT NULL")
//                .addColumn("z", "INT NOT NULL")
//                .addColumn("world", "VARCHAR(255) NOT NULL")
//                .addColumn("type", "VARCHAR(255) NOT NULL")
//                .addColumn("total", "DECIMAL(32,2) NOT NULL")
//                .addColumn("tax", "DECIMAL(32,2) NOT NULL")
//                .addColumn("amount", "INT(32) NOT NULL")
//                .addColumn("player", "VARCHAR(255) NOT NULL")
//                .setIndex(IndexType.PRIMARY_KEY, null, "time")
//                .setIndex(IndexType.INDEX, "player_based", "time", "x", "y", "z", "world", "player")
//                .setIndex(IndexType.INDEX, "type_based", "time", "x", "y", "z", "world", "type")
//                .build()
//                .execute(((exception, sqlAction) -> {
//                    if (exception != null) {
//                        plugin.getLogger().log(Level.WARNING, "Failed to create messages table! SQL:" + sqlAction.getSQLContent(), exception);
//                    }
//                }));
//    }
//
//    /**
//     * Creates the database table 'messages'
//     */
//    public void createMessagesTable() {
//        manager.createTable(prefix + "messages")
//                .addColumn("owner", "VARCHAR(255) NOT NULL")
//                .addColumn("message", "TEXT NOT NULL")
//                .addColumn("time", "BIGINT(32) NOT NULL")
//                .build()
//                .execute(((exception, sqlAction) -> {
//                    if (exception != null) {
//                        plugin.getLogger().log(Level.WARNING, "Failed to create messages table! SQL:" + sqlAction.getSQLContent(), exception);
//                    }
//                }));
//    }
//
//    public void createLogsTable() {
//        manager.createTable(prefix + "logs")
//                .addColumn("time", "BIGINT(32) NOT NULL")
//                .addColumn("classname", "TEXT NULL")
//                .addColumn("data", "LONGTEXT NULL")
//                .build()
//                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create logs table! SQL:" + sqlAction.getSQLContent(), exception)));
//    }
//
//    public void createExternalCacheTable() {
//        manager.createTable(prefix + "external_cache")
//                .addColumn("x", "INT(32) NOT NULL")
//                .addColumn("y", "INT(32) NOT NULL")
//                .addColumn("z", "INT(32) NOT NULL")
//                .addColumn("world", "VARCHAR(255) NOT NULL")
//                .addColumn("space", "INT NULL")
//                .addColumn("stock", "INT NULL")
//                .setIndex(IndexType.PRIMARY_KEY, null, "x", "y", "z", "world")
//                .build()
//                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create extrenal_cache table! SQL:" + sqlAction.getSQLContent(), exception)));
//    }
//
//    public void createMetadataTable() {
//        manager.createTable(prefix + "metadata")
//                .addColumn("key", "VARCHAR(255) NOT NULL")
//                .addColumn("value", "LONGTEXT NOT NULL")
//                .setIndex(IndexType.PRIMARY_KEY, null, "key")
//                .build()
//                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create metadata table! SQL:" + sqlAction.getSQLContent(), exception)));
//        try {
//            setDatabaseVersion(2);
//        } catch (SQLException e) {
//            plugin.getLogger().log(Level.WARNING, "Failed to update database version!", e);
//        }
//    }
//
//    public void createPlayerTable() {
//        manager.createTable(prefix + "players")
//                .addColumn("uuid", "VARCHAR(255) NOT NULL")
//                .addColumn("locale", "TEXT NOT NULL")
//                .setIndex(IndexType.PRIMARY_KEY, null, "uuid")
//                .build()
//                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create players table! SQL:" + sqlAction.getSQLContent(), exception)));
//    }
//
//    @Override
//    public void setPlayerLocale(@NotNull UUID uuid, @NotNull String locale) {
//        Log.debug("Update: " + uuid + " last locale to " + locale);
//        manager.createReplace(prefix + "players")
//                .setColumnNames("uuid", "locale")
//                .setParams(uuid.toString(), locale)
//                .executeAsync(integer -> {
//                }, (exception, sqlAction) -> {
//                    if (exception != null) {
//                        Log.debug("Failed to update player locale! Err: " + exception.getMessage() + "; SQL: " + sqlAction.getSQLContent());
//                    }
//                });
//    }
//
//    @Override
//    public void getPlayerLocale(@NotNull UUID uuid, @NotNull Consumer<Optional<String>> callback) {
//        manager.createQuery()
//                .inTable(prefix + "players")
//                .addCondition("uuid", uuid.toString())
//                .selectColumns("locale")
//                .build()
//                .executeAsync(sqlQuery -> {
//                            try (ResultSet set = sqlQuery.getResultSet()) {
//                                if (set.next()) {
//                                    callback.accept(Optional.of(set.getString("locale")));
//                                }
//                            }
//                        }, (exception, sqlAction) -> {
//                            callback.accept(Optional.empty());
//                            plugin.getLogger().log(Level.WARNING, "Failed to get player locale! SQL:" + sqlAction.getSQLContent(), exception);
//                        }
//                );
//    }
//
//    /**
//     * Verifies that all required columns exist.
//     */
//    public void checkColumns() throws SQLException {
//        plugin.getLogger().info("Checking and updating database columns, it may take a while...");
//        if (getDatabaseVersion() < 1) {
//            // QuickShop v4/v5 upgrade
//            // Call updater
//            setDatabaseVersion(1);
//        }
//        if (getDatabaseVersion() == 1) {
//            // QuickShop-Hikari 1.1.0.0
//            try {
//                manager.alterTable(prefix + "shops")
//                        .addColumn("name", "TEXT NULL")
//                        .execute();
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.INFO, "Failed to add name column to shops table! SQL: " + e.getMessage());
//            }
//            plugin.getLogger().info("[DatabaseHelper] Migrated to 1.1.0.0 data structure, version 2");
//            setDatabaseVersion(2);
//        }
//        if (getDatabaseVersion() == 2) {
//            // QuickShop-Hikari 2.0.0.0
//            try {
//                manager.alterTable(prefix + "shops")
//                        .addColumn("permission", "TEXT NULL")
//                        .execute();
//            } catch (SQLException e) {
//                plugin.getLogger().log(Level.INFO, "Failed to add name column to shops table! SQL: " + e.getMessage());
//            }
//            setDatabaseVersion(3);
//        }
//        plugin.getLogger().info("Finished!");
//    }
//
//    public void setDatabaseVersion(int version) throws SQLException {
//        manager.createReplace(prefix + "metadata")
//                .setColumnNames("key", "value")
//                .setParams("database_version", version)
//                .execute();
//    }
//
//    public int getDatabaseVersion() {
//        try (SQLQuery query = manager.createQuery().inTable(prefix + "metadata")
//                .addCondition("key", "database_version")
//                .selectColumns("value")
//                .build().execute(); ResultSet result = query.getResultSet()) {
//            if (!result.next()) {
//                return 0;
//            }
//            return Integer.parseInt(result.getString("value"));
//        } catch (SQLException e) {
//            Log.debug("Failed to getting database version! Err: " + e.getMessage());
//            return -1;
//        }
//    }
//
//    @Override
//    public void cleanMessage(long weekAgo) {
//        manager.createDelete(prefix + "messages")
//                .addCondition("time", "<", weekAgo)
//                .build()
//                .executeAsync();
//
//    }
//
//    @Override
//    public void cleanMessageForPlayer(@NotNull UUID player) {
//        manager.createDelete(prefix + "messages")
//                .addCondition("owner", player.toString())
//                .build().executeAsync();
//    }
//
//    @Override
//    public void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed) {
//        Location location = shop.getLocation();
//        String worldName = null;
//        if (shop.getLocation().getWorld() != null) {
//            worldName = shop.getLocation().getWorld().getName();
//        }
//        manager.createReplace(prefix + "shops")
//                .setColumnNames("owner", "price", "itemConfig",
//                        "x", "y", "z", "world", "unlimited", "type", "extra",
//                        "currency", "disableDisplay", "taxAccount", "inventorySymbolLink", "inventoryWrapperName",
//                        "permission"
//                )
//                .setParams(shop.getOwner(), shop.getPrice(), Util.serialize(shop.getItem()),
//                        location.getBlockX(),
//                        location.getBlockY(),
//                        location.getBlockZ(),
//                        worldName,
//                        shop.isUnlimited() ? 1 : 0,
//                        shop.getShopType().toID(),
//                        shop.saveExtraToYaml(),
//                        shop.getCurrency(),
//                        shop.isDisableDisplay(),
//                        shop.getTaxAccountActual(),
//                        plugin.getInventoryWrapperManager().mklink(shop.getInventory()),
//                        shop.getInventoryWrapperProvider(),
//                        JsonUtil.getGson().toJson(shop.getPermissionAudiences())
//                )
//                .executeAsync(integer -> {
//                }, ((exception, sqlAction) -> plugin.getLogger().log(Level.SEVERE, "Failed to create the shop " + shop + " cause database returns an error while executing create SQL: " + sqlAction.getSQLContent() + ". The shop may loss after server restart!", exception)));
//    }
//
//    @Override
//    public void removeShop(@NotNull Shop shop) {
//        removeShop(shop.getLocation().getWorld().getName(),
//                shop.getLocation().getBlockX(),
//                shop.getLocation().getBlockY(),
//                shop.getLocation().getBlockZ());
//    }
//
//    @Override
//    public void removeShop(@NotNull String world, int x, int y, int z) {
//        manager.createDelete(prefix + "shops")
//                .addCondition("x", x)
//                .addCondition("y", y)
//                .addCondition("z", z)
//                .addCondition("world", world)
//                .build()
//                .executeAsync((handler) -> Log.debug("Operation completed, Remove shop for w:" + world + "x:" + x + ",y:" + y + ",z:" + z + ", " + handler + " lines affected"));
//    }
//
//    @Override
//    public @NotNull SQLQuery selectAllMessages() throws SQLException {
//        return selectTable("messages");
//    }
//
//    @Override
//    public @NotNull SQLQuery selectTable(@NotNull String table) throws SQLException {
//        return manager.createQuery()
//                .inTable(prefix + table)
//                .build()
//                .execute();
//    }
//
//    @Override
//    public @NotNull SQLQuery selectAllShops() throws SQLException {
//        return selectTable("shops");
//    }
//
//    @Override
//    public void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time) {
//        manager.createInsert(prefix + "messages")
//                .setColumnNames("owner", "message", "time")
//                .setParams(player.toString(), message, time)
//                .executeAsync((handler) -> Log.debug("Operation completed, saveOfflineTransaction for " + player + ", " + handler + " lines affected"));
//    }
//
//    @Override
//    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
//        manager.createUpdate(prefix + "shops")
//                .addColumnValue("owner", ownerUUID)
//                .addCondition("x", x)
//                .addCondition("y", y)
//                .addCondition("z", z)
//                .addCondition("world", worldName)
//                .build().executeAsync((handler) -> Log.debug("Operation completed, updateOwner2UUID " + ownerUUID + ", " + handler + "lines affected"));
//    }
//
//    @Override
//    public void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock) {
//        manager.createReplace(prefix + "external_cache")
//                .setColumnNames("x", "y", "z", "world", "space", "stock")
//                .setParams(shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ(), shop.getLocation().getWorld().getName(), space, stock)
//                .executeAsync();
//    }
//
//
//    @Override
//    public void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
//                           double price, int x, int y, int z, @NotNull String world, @NotNull String extra,
//                           @Nullable String currency, boolean disableDisplay, @Nullable String taxAccount,
//                           @NotNull String inventorySymbolLink, @NotNull String inventoryWrapperName, @Nullable String shopName,
//                           @NotNull Map<UUID, String> playerGroups) {
//        Log.debug("Shop updating: " + x + "," + y + "," + z + "," + world + ", " + inventorySymbolLink + ", " + inventoryWrapperName);
//        manager.createUpdate(prefix + "shops")
//                .addColumnValue("owner", owner)
//                .addColumnValue("itemConfig", Util.serialize(item))
//                .addColumnValue("unlimited", unlimited)
//                .addColumnValue("type", shopType)
//                .addColumnValue("price", price)
//                .addColumnValue("extra", extra)
//                .addColumnValue("currency", currency)
//                .addColumnValue("disableDisplay", disableDisplay ? 1 : 0)
//                .addColumnValue("taxAccount", taxAccount)
//                .addColumnValue("inventorySymbolLink", inventorySymbolLink)
//                .addColumnValue("inventoryWrapperName", inventoryWrapperName)
//                .addColumnValue("name", shopName)
//                .addColumnValue("permission", JsonUtil.getGson().toJson(playerGroups))
//                .addCondition("x", x)
//                .addCondition("y", y)
//                .addCondition("z", z)
//                .addCondition("world", world)
//                .build()
//                .executeAsync(null, (exception, sqlAction) -> {
//                    if (exception != null) {
//                        Log.debug("Failed to execute SQL:" + sqlAction.getSQLContent() + ", error: " + exception.getMessage());
//                    }
//                });
//    }
//
//    @Override
//    public void insertHistoryRecord(@NotNull Object rec) {
//        manager.createInsert(prefix + "logs")
//                .setColumnNames("time", "classname", "data")
//                .setParams(System.currentTimeMillis(), rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
//                .executeAsync();
//    }
//
//    @Override
//    public void insertMetricRecord(@NotNull ShopMetricRecord record) {
//        manager.createInsert(prefix + "metrics")
//                .setColumnNames("time", "x", "y", "z", "world", "type", "total", "tax", "amount", "player")
//                .setParams(record.getTime(),
//                        record.getX(),
//                        record.getY(),
//                        record.getZ(),
//                        record.getWorld(),
//                        record.getType().name(),
//                        record.getTotal(),
//                        record.getTax(),
//                        record.getAmount(),
//                        record.getPlayer().toString())
//                .executeAsync();
//    }
//
//    /**
//     * Returns true if the table exists
//     *
//     * @param table The table to check for
//     * @return True if the table is found
//     * @throws SQLException Throw exception when failed execute somethins on SQL
//     */
//    public boolean hasTable(@NotNull String table) throws SQLException {
//        Connection connection = manager.getConnection();
//        boolean match = false;
//        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
//            while (rs.next()) {
//                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
//                    match = true;
//                    break;
//                }
//            }
//        } finally {
//            connection.close();
//        }
//        return match;
//    }
//
//    /**
//     * Returns true if the given table has the given column
//     *
//     * @param table  The table
//     * @param column The column
//     * @return True if the given table has the given column
//     * @throws SQLException If the database isn't connected
//     */
//    public boolean hasColumn(@NotNull String table, @NotNull String column) throws SQLException {
//        if (!hasTable(table)) {
//            return false;
//        }
//        String query = "SELECT * FROM " + table + " LIMIT 1";
//        boolean match = false;
//        try (Connection connection = manager.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
//            ResultSetMetaData metaData = rs.getMetaData();
//            for (int i = 1; i <= metaData.getColumnCount(); i++) {
//                if (metaData.getColumnLabel(i).equals(column)) {
//                    match = true;
//                    break;
//                }
//            }
//        } catch (SQLException e) {
//            return match;
//        }
//        return match; // Uh, wtf.
//    }
//
//    public SQLManager getManager() {
//        return manager;
//    }
//
//    public String getPrefix() {
//        return prefix;
//    }
//}
