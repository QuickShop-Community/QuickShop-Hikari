package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.builder.TableQueryBuilder;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopModerator;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.database.bean.IsolatedScanResult;
import com.ghostchu.quickshop.database.bean.SimpleDataRecord;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.shop.SimpleShopModerator;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.reflect.TypeToken;
import lombok.Data;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.relique.jdbc.csv.CsvDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Date;
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
        manager.setDebugMode(Util.isDevMode());
        checkTables();
        checkColumns();
    }

    public void checkTables() throws SQLException {
        DataTables.initializeTables(manager, prefix);
    }

    @Override
    public void setPlayerLocale(@NotNull UUID uuid, @NotNull String locale) {
        Log.debug("Update: " + uuid + " last locale to " + locale);
        DataTables.PLAYERS.createReplace()
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
        DataTables.PLAYERS.createQuery()
                .addCondition("uuid", uuid.toString())
                .selectColumns("locale")
                .setLimit(1)
                .build()
                .executeAsync(sqlQuery -> {
                            ResultSet set = sqlQuery.getResultSet();
                            if (set.next()) {
                                callback.accept(Optional.of(set.getString("locale")));
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
                doV2Migrate();
                setDatabaseVersion(4);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (getDatabaseVersion() < 8) {
            try {
                plugin.getLogger().info("Data upgrading: Performing purge isolated data...");
                purgeIsolatedData();
                plugin.getLogger().info("Data upgrading: All completed!");
                setDatabaseVersion(8);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Finished!");
    }


    public void setDatabaseVersion(int version) throws SQLException {
        DataTables.METADATA
                .createReplace()
                .setColumnNames("key", "value")
                .setParams("database_version", version)
                .execute();
    }

    public int getDatabaseVersion() {
        try (SQLQuery query = DataTables.METADATA
                .createQuery()
                .addCondition("key", "database_version")
                .selectColumns("value")
                .setLimit(1)
                .build().execute()) {
            ResultSet result = query.getResultSet();
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
        DataTables.MESSAGES.createDelete()
                .addTimeCondition("time", -1L, weekAgo)
                .build()
                .executeAsync((handler) -> Log.debug("Operation completed, clean outdated messages for " + weekAgo + " weeks ago, " + handler + " lines affected"));
    }

    @Override
    public void cleanMessageForPlayer(@NotNull UUID player) {
        DataTables.MESSAGES.createDelete()
                .addCondition("receiver", player.toString())
                .build().executeAsync((handler) -> Log.debug("Operation completed, clean messages for " + player + ", " + handler + " lines affected"));
    }

    @Override
    public long createData(@NotNull Shop shop) throws SQLException {
        SimpleDataRecord simpleDataRecord = ((ContainerShop) shop).createDataRecord();
        long id = queryDataId(simpleDataRecord);

        if (id < 1) {
            Map<String, Object> map = simpleDataRecord.generateParams();
            return DataTables.DATA.createInsert()
                    .setColumnNames(new ArrayList<>(map.keySet()))
                    .setParams(map.values())
                    .returnGeneratedKey(Long.class)
                    .execute();
        } else {
            return id;
        }
    }

    @Override
    public DataRecord getDataRecord(long dataId) throws SQLException {
        try (SQLQuery query = DataTables.DATA.createQuery()
                .addCondition("id", dataId)
                .setLimit(1)
                .build().execute()) {
            ResultSet result = query.getResultSet();
            if (result.next()) {
                return new SimpleDataRecord(result);
            }
            return null;
        }
    }

    @Override
    public long createShop(long dataId) throws SQLException {
        Validate.isTrue(dataId > 0, "Data ID must be greater than 0!");
        return DataTables.SHOPS.createInsert()
                .setColumnNames("data")
                .setParams(dataId)
                .returnGeneratedKey(Long.class)
                .execute();
    }

    @Override
    public void createShopMap(long shopId, @NotNull Location location) throws SQLException {
        Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
        DataTables.SHOP_MAP.createReplace()
                .setColumnNames("world", "x", "y", "z", "shop")
                .setParams(location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        shopId)
                .execute();
    }

    @Override
    public void removeShopMap(@NotNull String world, int x, int y, int z) {
        // TODO: Execute isolated data check in async thread
        DataTables.SHOP_MAP.createDelete()
                .addCondition("world", world)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .build()
                .executeAsync();
    }

    @Override
    public void removeShop(long shopId) {
        Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
        DataTables.SHOPS.createDelete()
                .addCondition("id", shopId)
                .build().executeAsync();
    }

    @Override
    public void removeData(long dataId) {
        Validate.isTrue(dataId > 0, "Data ID must be greater than 0!");
        DataTables.DATA.createDelete()
                .addCondition("id", dataId)
                .build().executeAsync();
    }

    @Override
    public long locateShopId(@NotNull String world, int x, int y, int z) {
        try (SQLQuery query = DataTables.SHOP_MAP.createQuery()
                .addCondition("world", world)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .setLimit(1)
                .build().execute()) {
            ResultSet result = query.getResultSet();
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
        try (SQLQuery query = DataTables.SHOPS.createQuery()
                .addCondition("id", shopId)
                .setLimit(1)
                .build().execute()) {
            ResultSet result = query.getResultSet();
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
        return DataTables.MESSAGES.createQuery().build().execute();
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
        return DataTables.SHOP_MAP.createQuery().build().execute();
    }

    @Override
    public void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time) {
        DataTables.MESSAGES.createInsert()
                .setColumnNames("receiver", "time", "content")
                .setParams(player.toString(), new Date(time), message)
                .executeAsync((handler) -> Log.debug("Operation completed, saveOfflineTransaction for " + player + ", " + handler + " lines affected"));
    }

    @Override
    public void updateExternalInventoryProfileCache(long shopId, int space, int stock) {
        Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
        DataTables.EXTERNAL_CACHE.createReplace()
                .setColumnNames("shop", "space", "stock")
                .setParams(shopId, space, stock)
                .executeAsync((handler) -> Log.debug("Operation completed, update inventory status for shopId=" + shopId + ", " + handler + " lines affected"));
    }

    @Override
    public void updateShop(@NotNull Shop shop, @NotNull Consumer<Exception> callback) {
        Util.asyncThreadRun(() -> {
            SimpleDataRecord simpleDataRecord = ((ContainerShop) shop).createDataRecord();
            Location loc = shop.getLocation();
            // check if datarecord exists
            long shopId = shop.getShopId();
            if (shopId < 1) {
                Log.debug("Warning: Failed to update shop because the shop id locate result for " + loc + ", because the query shopId is " + shopId);
            } else {
                // Check if any data record already exists
                // TODO: Combine to one SQL (query -> exists return id -> not exists create)
                long dataId = queryDataId(simpleDataRecord);
                if (dataId > 0) {
                    DataTables.SHOPS.createUpdate()
                            .addCondition("id", shopId)
                            .setColumnValues("data", dataId)
                            .build()
                            .executeAsync(handler -> Log.debug("Operation completed, updateShop " + shop + ", " + handler + " lines affected"));
                } else {
                    long newDataId;
                    try {
                        newDataId = createData(shop);
                        DataTables.SHOPS.createUpdate()
                                .addCondition("id", shopId)
                                .addColumnValue("data", newDataId)
                                .build()
                                .executeAsync(handler -> Log.debug("Operation completed, updateShop " + shop + ", " + handler + " lines affected"));
                    } catch (SQLException e) {
                        callback.accept(e);
                    }
                }
            }
            callback.accept(null);
        });


    }

    public long queryDataId(@NotNull SimpleDataRecord simpleDataRecord) {
        // Check if dataRecord exists in database with same values
        Map<String, Object> lookupParams = simpleDataRecord.generateLookupParams();
        TableQueryBuilder builder = DataTables.DATA.createQuery();
        builder.setLimit(1);
        for (Map.Entry<String, Object> entry : lookupParams.entrySet()) {
            builder.addCondition(entry.getKey(), entry.getValue());
        }
        try (SQLQuery query = builder.build().execute()) {
            ResultSet set = query.getResultSet();
            if (set.next()) {
                int id = set.getInt("id");
                Log.debug("Found data record with id " + id + " for record " + simpleDataRecord);
                return id;
            }
            Log.debug("No data record found for record basic data: " + simpleDataRecord);
            return 0;
        } catch (SQLException e) {
            Log.debug("Failed to query data record for " + simpleDataRecord + " Err: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void insertHistoryRecord(@NotNull Object rec) {
        DataTables.LOG_OTHERS.createInsert()
                .setColumnNames("type", "data")
                .setParams(rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
                .executeAsync((handler) -> Log.debug("Operation completed, insertHistoryRecord, " + handler + " lines affected"));
    }

    @Override
    public void insertMetricRecord(@NotNull ShopMetricRecord record) {
        Util.asyncThreadRun(() -> {
            long dataId = plugin.getDatabaseHelper().locateShopDataId(record.getShopId());
            DataTables.LOG_PURCHASE
                    .createInsert()
                    .setColumnNames("time", "shop", "data", "buyer", "type", "amount", "money", "tax")
                    .setParams(new Date(record.getTime()), record.getShopId()
                            , dataId, record.getPlayer(), record.getType().name(),
                            record.getAmount(), record.getTotal(), record.getTax());
        });
    }

    @Override
    public void insertTransactionRecord(@Nullable UUID from, @Nullable UUID to, double amount, @Nullable String currency, double taxAmount, @Nullable UUID taxAccount, @Nullable String error) {
        if (from == null) {
            from = Util.getNilUniqueId();
        }
        if (to == null) {
            to = Util.getNilUniqueId();
        }
        DataTables.LOG_TRANSACTION.createInsert()
                .setColumnNames("from", "to", "currency", "amount", "tax_amount", "tax_account", "error")
                .setParams(from.toString(), to.toString(), currency, amount, taxAmount, taxAccount == null ? null : taxAccount.toString(), error)
                .executeAsync((handler) -> Log.debug("Operation completed, insertTransactionRecord, " + handler + " lines affected"));
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

    public @NotNull SQLManager getManager() {
        return manager;
    }

    public @NotNull String getPrefix() {
        return prefix;
    }

    private boolean silentTableMoving(@NotNull String originTableName, @NotNull String newTableName) {
        try {
            if(hasTable(originTableName)) {
                if (plugin.getDatabaseDriverType() == QuickShop.DatabaseDriverType.MYSQL) {
                    manager.executeSQL("CREATE TABLE " + newTableName + " SELECT * FROM " + originTableName);
                } else {
                    manager.executeSQL("CREATE TABLE " + newTableName + " AS SELECT * FROM " + originTableName);
                }
                manager.executeSQL("DROP TABLE " + originTableName);
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }


    private void doV2Migrate() throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        plugin.getLogger().info("Please wait... QuickShop-Hikari preparing for database migration...");
        String actionId = UUID.randomUUID().toString().replace("-", "");
        plugin.getLogger().info("Action ID: " + actionId);

        plugin.getLogger().info("Cloning the tables for data copy...");
        if (!silentTableMoving(getPrefix() + "shops", getPrefix() + "shops_" + actionId)) {
            throw new IllegalStateException("Cannot rename critical tables");
        }
        // Backup tables
        silentTableMoving(getPrefix() + "messages", getPrefix() + "messages_" + actionId);
        silentTableMoving(getPrefix() + "logs", getPrefix() + "logs_" + actionId);
        silentTableMoving(getPrefix() + "external_cache", getPrefix() + "external_cache_" + actionId);
        silentTableMoving(getPrefix() + "player", getPrefix() + "player_" + actionId);
        silentTableMoving(getPrefix() + "metrics", getPrefix() + "metrics_" + actionId);
        plugin.getLogger().info("Cleaning resources...");
        // Backup current ver tables to prevent last converting failure or data loss
        for (DataTables value : DataTables.values()) {
            silentTableMoving(value.getName(), value.getName() + "_" + actionId);
        }
        plugin.getLogger().info("Ensuring shops ready for migrate...");
        if (!hasTable(getPrefix() + "shops_" + actionId)) {
            throw new IllegalStateException("Failed to rename tables!");
        }

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
        plugin.getLogger().info("Rebuilding database structure...");
        // Create new tables
        Log.debug("Table prefix: " + getPrefix());
        Log.debug("Global prefix: " + plugin.getDbPrefix());
        DataTables.initializeTables(manager, getPrefix());
        plugin.getLogger().info("Validating tables exists...");
        for (DataTables value : DataTables.values()) {
            if (!value.isExists()) {
                throw new IllegalStateException("Table " + value.getName() + " doesn't exists even rebuild structure!");
            }
        }

        for (OldShopData data : oldShopData) {
            long dataId = DataTables.DATA.createInsert()
                    .setColumnNames("owner", "item", "name", "type", "currency", "price", "unlimited", "hologram", "tax_account", "permissions", "extra", "inv_wrapper", "inv_symbol_link")
                    .setParams(data.owner, data.itemConfig, data.name, data.type, data.currency, data.price, data.unlimited, data.disableDisplay, data.taxAccount, JsonUtil.getGson().toJson(data.permission), data.extra, data.inventoryWrapperName, data.inventorySymbolLink)
                    .returnGeneratedKey(Long.class)
                    .execute();
            if (dataId < 1) {
                throw new IllegalStateException("DataId creation failed.");
            }
            long shopId = DataTables.SHOPS.createInsert()
                    .setColumnNames("data").setParams(dataId)
                    .returnGeneratedKey(Long.class).execute();
            if (shopId < 1) {
                throw new IllegalStateException("ShopId creation failed.");
            }
            DataTables.SHOP_MAP.createReplace()
                    .setColumnNames("world", "x", "y", "z", "shop")
                    .setParams(data.world, data.x, data.y, data.z, shopId)
                    .execute();
            plugin.getLogger().info("Converting shops...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldMessageData.size();
        for (OldMessageData data : oldMessageData) {
            DataTables.MESSAGES.createInsert()
                    .setColumnNames("receiver", "time", "content")
                    .setParams(data.owner, data.time, data.message)
                    .execute();
            plugin.getLogger().info("Converting messages...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldPlayerData.size();
        for (OldPlayerData data : oldPlayerData) {
            DataTables.PLAYERS.createInsert()
                    .setColumnNames("uuid", "locale")
                    .setParams(data.uuid, data.locale)
                    .execute();
            plugin.getLogger().info("Converting players properties...  (" + (++pos) + "/" + total + ")");
        }
        pos = 0;
        total = oldMetricData.size();
        for (OldShopMetricData data : oldMetricData) {
            long shopId = locateShopId(data.getWorld(), data.getX(), data.getY(), data.getZ());
            if (shopId < 1) {
                throw new IllegalStateException("ShopId not found.");
            }
            long dataId = locateShopDataId(shopId);
            if (dataId < 1) {
                throw new IllegalStateException("DataId not found.");
            }
            DataTables.LOG_PURCHASE.createInsert()
                    .setColumnNames("time", "shop", "data", "buyer", "type", "amount", "money", "tax")
                    .setParams(data.time, shopId, dataId, data.player, data.type, data.amount, data.total, data.tax)
                    .execute();
            plugin.getLogger().info("Converting purchase metric...  (" + (++pos) + "/" + total + ")");
        }
        checkTables();
        plugin.getLogger().info("Migrate completed, previous versioned data was renamed to <PREFIX>_<TABLE_NAME>_<ACTION_ID>.");
    }

    private <T> void downloadData(@NotNull String name, @NotNull String tableLegacyName, @NotNull String actionId, @NotNull Class<T> clazz, @NotNull List<T> target) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        plugin.getLogger().info("Performing query for data downloading (" + name + ")...");
        if (hasTable(getPrefix() + tableLegacyName + "_" + actionId)) {
            try (SQLQuery query = manager.createQuery().inTable(getPrefix() + tableLegacyName + "_" + actionId)
                    .build().execute()) {
                ResultSet set = query.getResultSet();
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

    @NotNull
    public IsolatedScanResult<Long> purgeIsolatedData() throws SQLException {
        IsolatedScanResult<Long> shopIds = scanIsolatedShopIds();
        purgeShopTableIsolatedData(shopIds);
        IsolatedScanResult<Long> dataIds = scanIsolatedDataIds();
        purgeDataTableIsolatedData(dataIds);
        List<Long> total = new LinkedList<>();
        total.addAll(shopIds.getTotal());
        total.addAll(dataIds.getTotal());
        List<Long> isolated = new LinkedList<>();
        isolated.addAll(shopIds.getIsolated());
        isolated.addAll(dataIds.getIsolated());
        return new IsolatedScanResult<>(total, isolated);
    }

    public void purgeDataTableIsolatedData(@NotNull IsolatedScanResult<Long> toPurge) throws SQLException {
        plugin.getLogger().info("Pulling isolated DATA_ID data...");
        plugin.getLogger().info("Purging " + toPurge.getIsolated().size() + " isolated DATA_ID data...");
        for (long dataId : toPurge.getIsolated()) {
            int line = DataTables.DATA.createDelete().addCondition("id", dataId).build().execute();
            Log.debug("Purged data_id=" + dataId + ", " + line + " rows effected.");
        }
        plugin.getLogger().info("Purging completed.");
    }

    @NotNull
    public IsolatedScanResult<Long> scanIsolatedDataIds() throws SQLException {
        List<Long> dataIds = new LinkedList<>();
        List<Long> toPurge = new LinkedList<>();
        try (SQLQuery query = DataTables.DATA.createQuery().selectColumns("id").build().execute()) {
            ResultSet set = query.getResultSet();
            while (set.next()) {
                dataIds.add(set.getLong("id"));
            }
        }
        for (long dataId : dataIds) {
            if (checkIdUsage(DataTables.SHOPS, "data", dataId)) {
                continue;
            }
            if (checkIdUsage(DataTables.LOG_PURCHASE, "data", dataId)) {
                continue;
            }
            if (checkIdUsage(DataTables.LOG_OTHERS, "data", dataId)) {
                continue;
            }
            toPurge.add(dataId);
        }
        return new IsolatedScanResult<>(dataIds, toPurge);
    }

    public void purgeShopTableIsolatedData(@NotNull IsolatedScanResult<Long> toPurge) throws SQLException {
        plugin.getLogger().info("Pulling isolated SHOP_ID data...");
        plugin.getLogger().info("Purging " + toPurge.getIsolated().size() + " isolated SHOP_ID data...");
        for (long shopId : toPurge.getIsolated()) {
            int shopRows = DataTables.SHOPS.createDelete().addCondition("id", shopId).build().execute();
            int cacheRows = DataTables.EXTERNAL_CACHE.createDelete().addCondition("shop", shopId).build().execute();
            Log.debug("Purged shop_id=" + shopId + ", " + (shopRows + cacheRows) + " rows affected.");
        }
        plugin.getLogger().info("Purging completed.");
    }

    @NotNull
    public IsolatedScanResult<Long> scanIsolatedShopIds() throws SQLException {
        List<Long> shopIds = new LinkedList<>();
        List<Long> toPurge = new LinkedList<>();
        try (SQLQuery query = DataTables.SHOPS.createQuery().selectColumns("id").build().execute()) {
            ResultSet set = query.getResultSet();
            while (set.next()) {
                shopIds.add(set.getLong("id"));
            }
        }
        plugin.getLogger().info("Total " + shopIds.size() + " data found.");
        for (long shopId : shopIds) {
            if (checkIdUsage(DataTables.SHOP_MAP, "shop", shopId)) {
                continue;
            }
            if (checkIdUsage(DataTables.LOG_PURCHASE, "shop", shopId)) {
                continue;
            }
            if (checkIdUsage(DataTables.LOG_CHANGES, "shop", shopId)) {
                continue;
            }
            toPurge.add(shopId);
        }
        return new IsolatedScanResult<>(shopIds, toPurge);
    }


    /**
     * DELETE unused data with related keys from query table.
     *
     * @param targetTable  the table to be cleaned
     * @param targetColumn the column to be cleaned
     * @param queryTable   Table used for keys' query
     * @param queryColumn  Column used for keys' query
     * @return the number of deleted rows
     */
    @SuppressWarnings("SQLInjection")
    public Integer clearUnusedData(@NotNull DataTables targetTable, @NotNull String targetColumn,
                                   @NotNull DataTables queryTable, @NotNull String queryColumn) {
        String sql = "DELETE FROM `%(targetTable)` WHERE NOT EXISTS (" +
                " SELECT `%(queryColumn)` FROM `%(queryTable)`" +
                " WHERE `%(queryTable)`.`%(queryColumn)` = `%(targetTable)`.`%(targetColumn)` " +
                ")";

        sql = sql.replace("%(targetTable)", targetTable.getName())
                .replace("%(queryTable)", queryTable.getName())
                .replace("%(targetColumn)", targetColumn)
                .replace("%(queryColumn)", queryColumn);

        return manager.executeSQL(sql);
    }

    /**
     * Check if specified id exists.
     *
     * @param targetTable the table to be cleaned
     * @param column      The column that will be checked
     * @param id          The id that will be checked
     */
    @SuppressWarnings("SQLInjection")
    public boolean checkIdUsage(@NotNull DataTables targetTable, @NotNull String column, long id) throws SQLException {
        try (SQLQuery queryTableResult = targetTable.createQuery()
                .addCondition(column, id)
                .selectColumns(column)
                .setLimit(1).build().execute()) {
            ResultSet set = queryTableResult.getResultSet();
            return set.next();
        }
    }

    public void writeToCSV(@NotNull ResultSet set, @NotNull File csvFile) throws SQLException, IOException {
        if (!csvFile.getParentFile().exists()) {
            csvFile.getParentFile().mkdirs();
        }
        if (!csvFile.exists()) {
            csvFile.createNewFile();
        }
        try (PrintStream stream = new PrintStream(csvFile)) {
            Log.debug("Writing to CSV file: " + csvFile.getAbsolutePath());
            CsvDriver.writeToCsv(set, stream, true);
        }
    }

    public void importFromCSV(@NotNull File zipFile, @NotNull DataTables table) throws SQLException, ClassNotFoundException {
        Log.debug("Loading CsvDriver...S");
        Class.forName("org.relique.jdbc.csv.CsvDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:relique:csv:zip:" + zipFile);
             Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                     ResultSet.CONCUR_READ_ONLY);
             ResultSet results = stmt.executeQuery("SELECT * FROM " + table.getName())) {
            ResultSetMetaData metaData = results.getMetaData();
            String[] columns = new String[metaData.getColumnCount()];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }
            Log.debug("Parsed " + columns.length + " columns: " + Util.array2String(columns));
            while (results.next()) {
                Object[] values = new String[columns.length];
                for (int i = 0; i < values.length; i++) {
                    Log.debug("Copying column: " + columns[i]);
                    values[i] = results.getObject(columns[i]);
                }
                Log.debug("Inserting row: " + Util.array2String(Arrays.stream(values).map(Object::toString).toArray(String[]::new)));
                table.createInsert()
                        .setColumnNames(columns)
                        .setParams(values)
                        .execute();
            }
        }
    }


    @Data
    static class OldShopData {
        private String owner;
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

        private Map<UUID, String> permission = new HashMap<>();

        public OldShopData(ResultSet set) throws Exception {
            String ownerData = set.getString("owner");
            if (!MsgUtil.isJson(ownerData)) {
                owner = set.getString("owner");
                Type t = new TypeToken<Map<UUID, String>>() {
                }.getType();
                Map<UUID, String> map = JsonUtil.getGson().fromJson(set.getString("permission"), t);
                if (map == null) {
                    permission = new HashMap<>();
                } else {
                    permission = new HashMap<>(map);
                }
            } else {
                Log.debug(Level.WARNING, "Found a data-record that data mismatch with excepted, fixing...");
                //noinspection deprecation
                ShopModerator simpleShopModeratorLegacy = SimpleShopModerator.deserialize(ownerData);
                owner = simpleShopModeratorLegacy.getOwner().toString();
                simpleShopModeratorLegacy.getStaffs().forEach(staff -> permission.put(staff, BuiltInShopPermissionGroup.STAFF.getNamespacedNode()));
            }
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
        }
    }

    @Data
    static class OldMessageData {
        private final String owner;
        private final String message;
        private final Date time;

        public OldMessageData(ResultSet set) throws SQLException {
            owner = set.getString("owner");
            message = set.getString("message");
            long timeStamp = set.getLong("time");
            time = new Date(timeStamp);
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
