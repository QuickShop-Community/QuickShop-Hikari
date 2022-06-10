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
import cc.carm.lib.easysql.api.builder.TableQueryBuilder;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.database.bean.DataRecord;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A Util to execute all SQLs.
 */
public class SimpleDatabaseHelperV2 implements DatabaseHelper {


    @NotNull
    private final SQLManager manager;

    @NotNull
    private final QuickShop plugin;

    @NotNull
    private final String prefix;

    public SimpleDatabaseHelperV2(@NotNull QuickShop plugin, @NotNull SQLManager manager, @NotNull String prefix) throws SQLException {
        this.plugin = plugin;
        this.manager = manager;
        this.prefix = prefix;
        checkTables();
        checkColumns();
    }

    public void checkTables() throws SQLException {
        DataTables.initializeTables(manager, prefix);
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
    }

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
//
//        DataTables.LOG_CHANGES.create(manager,prefix);
//
//
//
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

    @Override
    public void setPlayerLocale(@NotNull UUID uuid, @NotNull String locale) {
        Log.debug("Update: " + uuid + " last locale to " + locale);
        DataTables.PLAYERS.createReplace(manager)
                .setColumnNames("uuid", "locale")
                .setParams(uuid.toString(), locale)
                .executeAsync(integer -> {
                }, ((exception, sqlAction) -> {
                    if (exception != null) {
                        Log.debug("Failed to update player locale! Err: " + exception.getMessage() + "; SQL: " + sqlAction.getSQLContent());
                    }
                }));
    }

    @Override
    public void getPlayerLocale(@NotNull UUID uuid, @NotNull Consumer<Optional<String>> callback) {
        DataTables.PLAYERS.createQuery(manager)
                .addCondition("uuid", uuid.toString())
                .selectColumns("locale")
                .build()
                .executeAsync(sqlQuery -> {
                            try (ResultSet set = sqlQuery.getResultSet()) {
                                if (set.next()) {
                                    callback.accept(Optional.of(set.getString("locale")));
                                }
                            }
                        }, (exception, sqlAction) -> {
                            callback.accept(Optional.empty());
                            plugin.getLogger().log(Level.WARNING, "Failed to get player locale! SQL:" + sqlAction.getSQLContent(), exception);
                        }
                );
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
        }
        if (getDatabaseVersion() == 3) {
            try {
                doV2Migate();
                setDatabaseVersion(4);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Finished!");
    }


    public void setDatabaseVersion(int version) throws SQLException {
        DataTables.METADATA
                .createReplace(manager)
                .setColumnNames("key", "value")
                .setParams("database_version", version)
                .execute();
    }

    public int getDatabaseVersion() {
        try (SQLQuery query = DataTables.METADATA
                .createQuery(manager)
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

    @Override
    public void cleanMessage(long weekAgo) {
        DataTables.MESSAGES.createDelete(manager)
                .addCondition("time", "<", weekAgo)
                .build()
                .executeAsync();
    }

    @Override
    public void cleanMessageForPlayer(@NotNull UUID player) {
        DataTables.MESSAGES.createDelete(manager)
                .addCondition("owner", player.toString())
                .build().executeAsync();
    }

    @Override
    public long createData(@NotNull Shop shop) throws SQLException {
        DataRecord dataRecord = ((ContainerShop) shop).createDataRecord();
        long id = queryDataId(dataRecord);

        if (id < 1) {
            Map<String, Object> map = dataRecord.generateParams();
            return DataTables.DATA.createInsert(manager)
                    .setColumnNames(new ArrayList<>(map.keySet()))
                    .setParams(map.values())
                    .returnGeneratedKey()
                    .execute();
        } else {
            return id;
        }
    }

    @Override
    public long createShop(long dataId) throws SQLException {
        return DataTables.SHOPS.createReplace(manager)
                .setColumnNames("data")
                .setParams(dataId)
                .returnGeneratedKey()
                .execute();
    }

    @Override
    public void createShopMap(long shopId, @NotNull Location location) throws SQLException {
        DataTables.SHOP_MAP.createReplace(manager)
                .setColumnNames("world", "x", "y", "z", "shop")
                .setParams(location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        shopId)
                .returnGeneratedKey()
                .execute();
    }

    @Override
    public void removeShopMap(@NotNull String world, int x, int y, int z) throws SQLException {
        DataTables.SHOP_MAP.createDelete(manager)
                .addCondition("world", world)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .build()
                .execute();
    }

    @Override
    public void removeShop(long shopId) {
        DataTables.SHOPS.createDelete(manager)
                .addCondition("id", shopId)
                .build()
                .executeAsync();
    }

    @Override
    public void removeData(long dataId) {
        DataTables.DATA.createDelete(manager)
                .addCondition("id", dataId)
                .build()
                .executeAsync();
    }

    @Override
    public long locateShopId(@NotNull String world, int x, int y, int z) {
        try (SQLQuery query = DataTables.SHOP_MAP.createQuery(manager)
                .addCondition("world", world)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .build().execute(); ResultSet result = query.getResultSet()) {
            if (result.next()) {
                return result.getInt("shop");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            Log.debug("Failed to locate shop id! Err: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public long locateShopDataId(long shopId) {
        try (SQLQuery query = DataTables.SHOPS.createQuery(manager)
                .addCondition("id", shopId)
                .build().execute(); ResultSet result = query.getResultSet()) {
            if (result.next()) {
                return result.getInt("data");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public @NotNull SQLQuery selectAllMessages() throws SQLException {
        return selectTable("messages");
    }

    @Override
    public @NotNull SQLQuery selectTable(@NotNull String table) throws SQLException {
        return manager.createQuery()
                .inTable(prefix + table)
                .build()
                .execute();
    }

    @Override
    public @NotNull SQLQuery selectAllShops() throws SQLException {
        return selectTable("shops");
    }

    @Override
    public void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time) {
        manager.createInsert(prefix + "messages")
                .setColumnNames("owner", "message", "time")
                .setParams(player.toString(), message, time)
                .executeAsync((handler) -> Log.debug("Operation completed, saveOfflineTransaction for " + player + ", " + handler + " lines affected"));
    }

    @Override
    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
        manager.createUpdate(prefix + "shops")
                .addColumnValue("owner", ownerUUID)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .addCondition("world", worldName)
                .build().executeAsync((handler) -> Log.debug("Operation completed, updateOwner2UUID " + ownerUUID + ", " + handler + "lines affected"));
    }

    @Override
    public void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock) {
        manager.createReplace(prefix + "external_cache")
                .setColumnNames("x", "y", "z", "world", "space", "stock")
                .setParams(shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ(), shop.getLocation().getWorld().getName(), space, stock)
                .executeAsync();
    }

    @Override
    public void updateShop(@NotNull Shop shop) throws SQLException {
        DataRecord dataRecord = ((ContainerShop) shop).createDataRecord();
        Location loc = shop.getLocation();
        // check if datarecord exists
        long shopId = locateShopId(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (shopId < 1) {
            Log.debug("Warning: Failed to update shop because the shop id locate result for " + loc + ", because the query shopId is " + shopId);
        } else {
            // Check if any data record already exists
            long dataId = queryDataId(dataRecord);
            if (dataId > 0) {
                DataTables.SHOPS.createReplace(manager)
                        .setColumnNames("data")
                        .setParams(dataId)
                        .executeAsync(handler -> Log.debug("Operation completed, updateShop " + shop + ", " + handler + " lines affected"));
            } else {
                long newDataId = createData(shop);
                DataTables.SHOPS.createReplace(manager)
                        .setColumnNames("data")
                        .setParams(newDataId)
                        .executeAsync(handler -> Log.debug("Operation completed, updateShop " + shop + ", " + handler + " lines affected"));
            }
        }
    }

    public long queryDataId(@NotNull DataRecord dataRecord) {
        // Check if dataRecord exists in database with same values
        Map<String, Object> map = dataRecord.generateParams();
        TableQueryBuilder builder = DataTables.DATA.createQuery(manager);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.addCondition(entry.getKey(), entry.getValue());
        }
        try (SQLQuery query = builder.build().execute(); ResultSet set = query.getResultSet()) {
            if (set.next()) {
                int id = set.getInt("id");
                Log.debug("Found data record with id " + id + " for record " + dataRecord);
            }
            Log.debug("No data record found for record " + dataRecord);
            return 0;
        } catch (SQLException e) {
            Log.debug("Failed to query data record for " + dataRecord + " Err: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void insertHistoryRecord(@NotNull Object rec) {
        manager.createInsert(prefix + "logs")
                .setColumnNames("time", "classname", "data")
                .setParams(System.currentTimeMillis(), rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
                .executeAsync();
    }

    @Override
    public void insertMetricRecord(@NotNull ShopMetricRecord record) {
        manager.createInsert(prefix + "metrics")
                .setColumnNames("time", "x", "y", "z", "world", "type", "total", "tax", "amount", "player")
                .setParams(record.getTime(),
                        record.getX(),
                        record.getY(),
                        record.getZ(),
                        record.getWorld(),
                        record.getType().name(),
                        record.getTotal(),
                        record.getTax(),
                        record.getAmount(),
                        record.getPlayer().toString())
                .executeAsync();
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

    /**
     * Returns true if the given table has the given column
     *
     * @param table  The table
     * @param column The column
     * @return True if the given table has the given column
     * @throws SQLException If the database isn't connected
     */
    public boolean hasColumn(@NotNull String table, @NotNull String column) throws SQLException {
        if (!hasTable(table)) {
            return false;
        }
        String query = "SELECT * FROM " + table + " LIMIT 1";
        boolean match = false;
        try (Connection connection = manager.getConnection(); PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnLabel(i).equals(column)) {
                    match = true;
                    break;
                }
            }
        } catch (SQLException e) {
            return match;
        }
        return match; // Uh, wtf.
    }

    public SQLManager getManager() {
        return manager;
    }

    public String getPrefix() {
        return prefix;
    }


    private void doV2Migate() throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        plugin.getLogger().info("Please wait... QuickShop-Hikari preparing for database migration...");
        String actionId = UUID.randomUUID().toString().replace("-", "");
        plugin.getLogger().info("Action ID: " + actionId);
        plugin.getLogger().info("Cloning the tables for data copy...");
        manager.alterTable(getPrefix() + "shops")
                .renameTo(getPrefix() + "shops_" + actionId)
                .execute();

        try {
            manager.alterTable(getPrefix() + "messages")
                    .renameTo(getPrefix() + "messages_" + actionId)
                    .execute();
        } catch (SQLException ignored) {
            plugin.getLogger().log(Level.INFO, "Error while rename tables! CHECK:Recoverable + Skip-able, Skipping...");
        }
        try {
            manager.alterTable(getPrefix() + "logs")
                    .renameTo(getPrefix() + "logs_" + actionId)
                    .execute();
        } catch (SQLException ignored) {
            plugin.getLogger().log(Level.INFO, "Error while rename tables! CHECK:Recoverable + Skip-able, Skipping...");
        }
        try {
            manager.alterTable(getPrefix() + "external_cache")
                    .renameTo(getPrefix() + "external_cache_" + actionId)
                    .execute();
        } catch (SQLException ignored) {
            plugin.getLogger().log(Level.INFO, "Error while rename tables! CHECK:Recoverable + Skip-able, Skipping...");
        }
        try {
            manager.alterTable(getPrefix() + "player")
                    .renameTo(getPrefix() + "player_" + actionId)
                    .execute();
        } catch (SQLException ignored) {
            plugin.getLogger().log(Level.INFO, "Error while rename tables! CHECK:Recoverable + Skip-able, Skipping...");
        }
        try {
            manager.alterTable(getPrefix() + "metrics")
                    .renameTo(getPrefix() + "metrics_" + actionId)
                    .execute();
        } catch (SQLException ignored) {
            plugin.getLogger().log(Level.INFO, "Error while rename tables! CHECK:Recoverable + Skip-able, Skipping...");
        }

        plugin.getLogger().info("Ensuring everything getting ready...");
        if (!hasTable(getPrefix() + "shops_" + actionId)) {
            throw new IllegalStateException("Failed to rename tables!");
        }
        plugin.getLogger().info("Rebuilding database structure...");
        // Create new tables
        checkTables();
        plugin.getLogger().info("Downloading the data that need to converting to memory...");
        List<OldShopData> oldShopData = new LinkedList<>();
        List<OldMessageData> oldMessageData = new LinkedList<>();
        List<OldShopMetricData> oldMetricData = new LinkedList<>();
        List<OldPlayerData> oldPlayerData = new LinkedList<>();
        downloadData("Shops", "shops", actionId, OldShopData.class, oldShopData);
        downloadData("Messages", "messages", actionId, OldMessageData.class, oldMessageData);
        downloadData("Players Properties", "player", actionId, OldPlayerData.class, oldPlayerData);
        downloadData("Shop Metrics", "metric", actionId, OldShopMetricData.class, oldMetricData);
        plugin.getLogger().info("Converting data and write into database...");
        // Convert data
        int pos = 0;
        int total = oldShopData.size();
        for (OldShopData data : oldShopData) {
            long dataId = DataTables.DATA.createInsert(manager)
                    .setColumnNames("owner", "item", "name", "type", "currency", "price", "unlimited", "hologram", "tax_account", "permissions", "extra", "inv_wrapper", "inv_symbol_link")
                    .setParams(data.owner, data.itemConfig, data.name, data.type, data.currency, data.price, data.unlimited, data.disableDisplay, data.taxAccount, data.permission, data.extra, data.inventoryWrapperName, data.inventorySymbolLink)
                    .returnGeneratedKey()
                    .execute();
            if (dataId < 1)
                throw new IllegalStateException("DataId creation failed.");
            long shopId = DataTables.SHOPS.createInsert(manager)
                    .setColumnNames("data").setParams(dataId).execute();
            if (shopId < 1)
                throw new IllegalStateException("ShopId creation failed.");
            DataTables.SHOP_MAP.createReplace(manager)
                    .setColumnNames("world", "x", "y", "z", "shop")
                    .setParams(data.world, data.x, data.y, data.z, shopId)
                    .execute();
            plugin.getLogger().info("Converting shops...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldMessageData.size();
        for (OldMessageData data : oldMessageData) {
            DataTables.DATA.createInsert(manager)
                    .setColumnNames("receiver", "time", "content")
                    .setParams(data.owner, data.time, data.message)
                    .execute();
            plugin.getLogger().info("Converting messages...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldPlayerData.size();
        for (OldPlayerData data : oldPlayerData) {
            DataTables.DATA.createInsert(manager)
                    .setColumnNames("uuid", "locale")
                    .setParams(data.uuid, data.locale)
                    .execute();
            plugin.getLogger().info("Converting players properties...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldMetricData.size();
        for (OldShopMetricData data : oldMetricData) {
            long shopId = locateShopId(data.getWorld(), data.getX(), data.getY(), data.getZ());
            if (shopId < 1)
                throw new IllegalStateException("ShopId not found.");
            long dataId = locateShopDataId(shopId);
            if (dataId < 1)
                throw new IllegalStateException("DataId not found.");
            DataTables.LOG_PURCHASE.createInsert(manager)
                    .setColumnNames("time", "shop", "data", "buyer", "type", "amount", "money", "tax")
                    .setParams(data.time, shopId, dataId, data.player, data.type, data.amount, data.total, data.tax)
                    .execute();
            plugin.getLogger().info("Converting purchase metric...  (" + (++pos) + "/" + total + ")");
        }
        plugin.getLogger().info("Migrate completed, previous versioned data was renamed to <PREFIX>_<TABLE_NAME>_<ACTION_ID>.");
    }

    private <T> void downloadData(@NotNull String name, @NotNull String tableLegacyName, @NotNull String actionId, @NotNull Class<T> clazz, @NotNull List<T> target) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        plugin.getLogger().info("Performing query for data downloading (" + name + ")...");
        if (hasTable(getPrefix() + tableLegacyName + "_" + actionId)) {
            try (SQLQuery query = manager.createQuery().inTable(getPrefix() + tableLegacyName + "_" + actionId)
                    .build().execute(); ResultSet set = query.getResultSet()) {
                int count = 0;
                while (set.next()) {
                    target.add(clazz.getConstructor(ResultSet.class).newInstance(set));
                    count++;
                    plugin.getLogger().info("Downloaded " + count + " data to memory (" + name + ")...");
                }
                plugin.getLogger().info("Downloaded " + count + " total, completed. (" + name + ")");
            }
        } else {
            plugin.getLogger().info("Skipping for table " + tableLegacyName);
        }
    }

    @Data
    static class OldShopData {
        private final String owner;
        private final double price;
        private final String itemConfig;
        private final int x;
        private final int y;
        private final int z;
        private final String world;
        private final boolean unlimited;
        private final int type;
        private final String extra;
        private final String currency;
        private final boolean disableDisplay;
        private final String taxAccount;
        private final String inventorySymbolLink;
        private final String inventoryWrapperName;
        private final String name;

        private final String permission;

        public OldShopData(ResultSet set) throws SQLException {
            owner = set.getString("owner");
            price = set.getDouble("price");
            itemConfig = set.getString("itemConfig");
            x = set.getInt("x");
            y = set.getInt("y");
            z = set.getInt("z");
            world = set.getString("world");
            unlimited = set.getBoolean("unlimited");
            type = set.getInt("type");
            extra = set.getString("extra");
            currency = set.getString("currency");
            disableDisplay = set.getBoolean("disableDisplay");
            taxAccount = set.getString("taxAccount");
            inventorySymbolLink = set.getString("inventorySymbolLink");
            inventoryWrapperName = set.getString("inventoryWrapperName");
            name = set.getString("name");
            permission = set.getString("permission");

        }
    }

    @Data
    static class OldMessageData {
        private final String owner;
        private final String message;
        private final long time;

        public OldMessageData(ResultSet set) throws SQLException {
            owner = set.getString("owner");
            message = set.getString("message");
            time = set.getLong("time");
        }
    }

    @Data
    static class OldShopMetricData {
        private final long time;
        private final int x;
        private final int y;
        private final int z;
        private final String world;
        private final String type;
        private final double total;
        private final double tax;
        private final int amount;
        private final String player;

        public OldShopMetricData(ResultSet set) throws SQLException {
            time = set.getLong("time");
            x = set.getInt("x");
            y = set.getInt("y");
            z = set.getInt("z");
            world = set.getString("world");
            type = set.getString("type");
            total = set.getDouble("total");
            tax = set.getDouble("tax");
            amount = set.getInt("amount");
            player = set.getString("player");
        }
    }

    @Data
    static class OldPlayerData {
        private final String uuid;
        private final String locale;

        public OldPlayerData(ResultSet set) throws SQLException {
            uuid = set.getString("owner");
            locale = set.getString("locale");
        }
    }

}
