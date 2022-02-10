/*
 * This file is a part of project QuickShop, the name is SimpleDatabaseHelper.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.enums.IndexType;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopModerator;
import org.maxgamer.quickshop.metric.ShopMetricRecord;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A Util to execute all SQLs.
 */
public class SimpleDatabaseHelper implements DatabaseHelper, Reloadable {


    @NotNull
    private final SQLManager manager;

    @NotNull
    private final QuickShop plugin;

    public SimpleDatabaseHelper(@NotNull QuickShop plugin, @NotNull SQLManager manager) throws SQLException {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() throws SQLException {
        if (!hasTable(plugin.getDbPrefix() + "metadata")) {
            createMetadataTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "shops")) {
            createShopsTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "messages")) {
            createMessagesTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "logs")) {
            createLogsTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "external_cache")) {
            createExternalCacheTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "players")) {
            createPlayerTable();
        }
        if (!hasTable(plugin.getDbPrefix() + "metrics")) {
            createMetricsTable();
        }
        checkColumns();
    }

    /**
     * Creates the database table 'shops'.
     */

    public void createShopsTable() {
        manager.createTable(plugin.getDbPrefix() + "shops")
                .addColumn("owner", "VARCHAR(255) NOT NULL")
                .addColumn("price", "DOUBLE(32) NOT NULL")
                .addColumn("itemConfig", "LONGTEXT NOT NULL")
                .addColumn("x", "INTEGER(32) NOT NULL")
                .addColumn("y", "INTEGER(32) NOT NULL")
                .addColumn("z", "INTEGER(32) NOT NULL")
                .addColumn("world", "VARCHAR(255) NOT NULL")
                .addColumn("unlimited", "BOOLEAN NOT NULL")
                .addColumn("type", "INTEGER(8) NOT NULL")
                .addColumn("extra", "LONGTEXT NULL")
                .addColumn("currency", "TEXT NULL")
                .addColumn("disableDisplay", "INTEGER NULL DEFAULT 0")
                .addColumn("taxAccount", "VARCHAR(255) NULL")
                .addColumn("inventorySymbolLink", "TEXT NULL")
                .addColumn("inventoryWrapperName", "VARCHAR(255) NULL")
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
        manager.createTable(plugin.getDbPrefix() + "metrics")
                .addColumn("time", "BIGINT(32) NOT NULL")
                .addColumn("x", "INTEGER(32) NOT NULL")
                .addColumn("y", "INTEGER(32) NOT NULL")
                .addColumn("z", "INTEGER(32) NOT NULL")
                .addColumn("world", "VARCHAR(255) NOT NULL")
                .addColumn("type", "VARCHAR(255) NOT NULL")
                .addColumn("total", "DOUBLE(32) NOT NULL")
                .addColumn("tax", "DOUBLE(32) NOT NULL")
                .addColumn("amount", "INTEGER(32) NOT NULL")
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
        manager.createTable(plugin.getDbPrefix() + "messages")
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
        manager.createTable(plugin.getDbPrefix() + "logs")
                .addColumn("time", "BIGINT(32) NOT NULL")
                .addColumn("classname", "TEXT NULL")
                .addColumn("data", "LONGTEXT NULL")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create logs table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    public void createExternalCacheTable() {
        manager.createTable(plugin.getDbPrefix() + "external_cache")
                .addColumn("x", "INTEGER(32) NOT NULL")
                .addColumn("y", "INTEGER(32) NOT NULL")
                .addColumn("z", "INTEGER(32) NOT NULL")
                .addColumn("world", "VARCHAR(32) NOT NULL")
                .addColumn("space", "INTEGER NULL")
                .addColumn("stock", "INTEGER NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "x", "y", "z", "world")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create extrenal_cache table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    public void createMetadataTable() {
        manager.createTable(plugin.getDbPrefix() + "metadata")
                .addColumn("key", "VARCHAR(255) NOT NULL")
                .addColumn("value", "TEXT NOT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "key")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create metadata table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    public void createPlayerTable() {
        manager.createTable(plugin.getDbPrefix() + "players")
                .addColumn("uuid", "VARCHAR(255) NOT NULL")
                .addColumn("locale", "TEXT NOT NULL")
                .setIndex(IndexType.PRIMARY_KEY, null, "uuid")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create players table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    @Override
    public void setPlayerLocale(@NotNull UUID uuid, @NotNull String locale) {
        manager.createReplace(plugin.getDbPrefix() + "players")
                .setColumnNames("uuid", "locale")
                .setParams(uuid.toString(), locale)
                .executeAsync();
    }

    @Override
    public void getPlayerLocale(@NotNull UUID uuid, @NotNull Consumer<Optional<String>> callback) {
        manager.createQuery()
                .inTable(plugin.getDbPrefix() + "players")
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
    public void checkColumns() {
        plugin.getLogger().info("Checking and updating database columns, it may take a while...");
        if (getDatabaseVersion() == 0) {
            // QuickShop v4/v5 upgrade
            // Call updater
            setDatabaseVersion(1);
        }

        plugin.getLogger().info("Finished!");
    }

    public void setDatabaseVersion(int version) {
        manager.createReplace(plugin.getDbPrefix() + "metadata")
                .setColumnNames("key", "value")
                .setParams("database_version", version)
                .executeAsync();
    }

    public int getDatabaseVersion() {
        try (SQLQuery query = manager.createQuery().inTable(plugin.getDbPrefix() + "metadata")
                .addCondition("key", "database_version")
                .selectColumns("value")
                .build().execute(); ResultSet result = query.getResultSet()) {
            if (!result.next()) {
                return 0;
            }
            return Integer.parseInt(result.getString("version"));
        } catch (SQLException e) {
            return -1;
        }
    }

    @Override
    public void cleanMessage(long weekAgo) {
        manager.createDelete(plugin.getDbPrefix() + "messages")
                .addCondition("time", "<", weekAgo)
                .build()
                .executeAsync();

    }

    @Override
    public void cleanMessageForPlayer(@NotNull UUID player) {
        manager.createDelete(plugin.getDbPrefix() + "messages")
                .addCondition("owner", player.toString())
                .build().executeAsync();
    }

    @Override
    public void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed) {
        Location location = shop.getLocation();
        String worldName = null;
        if (shop.getLocation().getWorld() != null) {
            worldName = shop.getLocation().getWorld().getName();
        }
        manager.createReplace(plugin.getDbPrefix() + "shops")
                .setColumnNames("owner", "price", "itemConfig",
                        "x", "y", "z", "world", "unlimited", "type", "extra",
                        "currency","disableDisplay","taxAccount","inventorySymbolLink","inventoryWrapperName"
                )
                .setParams(ShopModerator.serialize(shop.getModerator()), shop.getPrice(), Util.serialize(shop.getItem()),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        worldName,
                        shop.isUnlimited() ? 1 : 0,
                        shop.getShopType().toID(),
                        shop.saveExtraToYaml(),
                        shop.getCurrency(),
                        shop.isDisableDisplay(),
                        shop.getTaxAccountActual(),
                        plugin.getInventoryWrapperManager().mklink(shop.getInventory()),
                        shop.getInventoryWrapperProvider()
                )
                .executeAsync(integer -> {
                    if (integer <= 0) {
                        plugin.getLogger().warning("SQL execution returns " + integer + " lines modified when creating shop " + shop + ", shop may not save correctly");
                    }
                }, ((exception, sqlAction) -> plugin.getLogger().log(Level.SEVERE, "Failed to create the shop " + shop + " cause database returns an error while executing create SQL: " + sqlAction.getSQLContent() + ". The shop may loss after server restart!", exception)));
    }

    @Override
    public void removeShop(Shop shop) {
        removeShop(shop.getLocation().getWorld().getName(),
                shop.getLocation().getBlockX(),
                shop.getLocation().getBlockY(),
                shop.getLocation().getBlockZ());
    }

    @Override
    public void removeShop(String world, int x, int y, int z) {
        manager.createDelete(plugin.getDbPrefix() + "shops")
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .addCondition("world", world)
                .build()
                .executeAsync();
    }

    @Override
    public SQLQuery selectAllMessages() throws SQLException {
        return selectTable("messages");
    }

    @Override
    public SQLQuery selectTable(String table) throws SQLException {
        return manager.createQuery()
                .inTable(plugin.getDbPrefix() + table)
                .build()
                .execute();
    }

    @Override
    public SQLQuery selectAllShops() throws SQLException {
        return selectTable("shops");
    }

    @Override
    public void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time) {
        manager.createInsert(plugin.getDbPrefix() + "messages")
                .setColumnNames("owner", "message", "time")
                .setParams(player.toString(), message, time)
                .executeAsync();
    }

    @Override
    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
        manager.createUpdate(plugin.getDbPrefix() + "shops")
                .setColumnValues("owner", ownerUUID)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .addCondition("world", worldName)
                .build().executeAsync();
    }

    @Override
    public void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock) {
        manager.createReplace(plugin.getDbPrefix() + "external_cache")
                .setColumnNames("x", "y", "z", "world", "space", "stock")
                .setParams(shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ(), shop.getLocation().getWorld().getName(), space, stock)
                .executeAsync();
    }


    @Override
    public void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                           double price, int x, int y, int z, @NotNull String world, @NotNull String extra,
                           @Nullable String currency, boolean disableDisplay, @Nullable String taxAccount,
                           @NotNull String inventorySymbolLink, @NotNull String inventoryWrapperName) {
        manager.createUpdate(plugin.getDbPrefix() + "shops")
                .setColumnValues("owner", owner)
                .setColumnValues("itemConfig", Util.serialize(item))
                .setColumnValues("unlimited", unlimited)
                .setColumnValues("type", shopType)
                .setColumnValues("price", price)
                .setColumnValues("extra", extra)
                .setColumnValues("disableDisplay", disableDisplay ? 1 : 0)
                .setColumnValues("taxAccount", taxAccount)
                .setColumnValues("inventorySymbolLink",inventorySymbolLink)
                .setColumnValues("inventoryWrapperName",inventoryWrapperName)
                .addCondition("x", x)
                .addCondition("y", y)
                .addCondition("z", z)
                .addCondition("world", world)
                .build()
                .executeAsync(null, (exception, sqlAction) -> {
                    if (exception != null) {
                        plugin.getLogger().severe("Error updating shop: " + exception.getMessage() + ", changes may loss after server restart.");
                        Util.debugLog("Failed to execute SQL:" + sqlAction.getSQLContent() + ", error: " + exception.getMessage());
                    }
                });
    }

    @Override
    public void insertHistoryRecord(@NotNull Object rec) {
        manager.createInsert(plugin.getDbPrefix() + "logs")
                .setColumnNames("time", "classname", "data")
                .setParams(System.currentTimeMillis(), rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
                .executeAsync();
    }
    @Override
    public void insertMetricRecord(@NotNull ShopMetricRecord record) {
        manager.createInsert(plugin.getDbPrefix() + "metrics")
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
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
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
}
