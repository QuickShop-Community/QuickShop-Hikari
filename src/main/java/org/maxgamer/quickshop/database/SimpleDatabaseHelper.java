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
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;

import java.sql.*;
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
        checkColumns();
    }

    /**
     * Creates the database table 'shops'.
     */

    private void createShopsTable() {
        manager.createTable(plugin.getDbPrefix() + "shops")
                .addColumn("owner", "VARCHAR(255) NOT NULL")
                .addColumn("price", "DOUBLE(32,2) NOT NULL")
                .addColumn("itemConfig", "LONGTEXT NOT NULL")
                .addColumn("x", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("y", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("z", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("world", "VARCHAR(255) NOT NULL PRIMARY KEY")
                .addColumn("unlimited", "BOOLEAN NOT NULL")
                .addColumn("type", "INTEGER(8) NOT NULL")
                .addColumn("extra", "LONGTEXT NULL")
                .addColumn("currency", "TEXT NULL")
                .addColumn("disableDisplay", "INTEGER NULL DEFAULT -1")
                .addColumn("taxAccount", "VARCHAR(255) NULL")
                .build().execute((i) -> {
                    return i;
                    // Do nothing
                }, ((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed while trying create the shop table! SQL: " + sqlAction.getSQLContent(), exception)));
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .modifyColumn("itemConfig", "CHARACTER SET utf8mb4 NOT NULL")
                .execute(null);
    }

    /**
     * Creates the database table 'messages'
     */
    private void createMessagesTable() {
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

    private void createLogsTable() {
        manager.createTable(plugin.getDbPrefix() + "logs")
                .addColumn("time", "BIGINT(32) NOT NULL")
                .addColumn("classname", "TEXT NULL")
                .addColumn("data", "LONGTEXT NULL")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create logs table! SQL:" + sqlAction.getSQLContent(), exception)));
    }

    private void createExternalCacheTable() {
        String createTable = "CREATE TABLE " + plugin.getDbPrefix()
                + "external_cache  (x INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, PRIMARY KEY (x, y, z, world));";
        manager.createTable(plugin.getDbPrefix() + "external_cache")
                .addColumn("x", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("y", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("z", "INTEGER(32) NOT NULL PRIMARY KEY")
                .addColumn("world", "VARCHAR(32) NOT NULL PRIMARY KEY")
                .addColumn("space", "INTEGER NULL")
                .addColumn("stock", "INTEGER NULL")
                .build()
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create extrenal_cache table! SQL:" + sqlAction.getSQLContent(), exception)));
    }


    /**
     * Verifies that all required columns exist.
     */
    private void checkColumns() {
        plugin.getLogger().info("Checking and updating database columns, it may take a while...");
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .addColumn("currency", "TEXT NULL")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create extra column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .addColumn("extra", "LONGTEXT NULL")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create currency column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .addColumn("disableDisplay", "int NULL DEFAULT -1")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create currency column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .addColumn("taxAccount", "VARCHAR(255) NULL")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to create currency column! SQL:" + sqlAction.getSQLContent(), exception)
                ));

        // V3.4.2
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .modifyColumn("price", "double(32,2) NOT NULL AFTER owner")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to update price column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
        // V3.4.3
        manager.alterTable(plugin.getDbPrefix() + "message")
                .modifyColumn("time", "BIGINT(32) NOT NULL AFTER message")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to update time column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
        //Extra column
        // Alter table, migrate from boolean to int to storage more types
        manager.alterTable(plugin.getDbPrefix() + "shops")
                .modifyColumn("type", "INTEGER(8) NOT NULL")
                .execute(((exception, sqlAction) -> plugin.getLogger().log(Level.WARNING, "Failed to update type column! SQL:" + sqlAction.getSQLContent(), exception)
                ));
//
//        if (manager.getDatabase() instanceof MySQLCore) {
//            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
//                    .getDbPrefix() + "messages MODIFY COLUMN message text CHARACTER SET utf8mb4 NOT NULL AFTER owner", checkTask));
//            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
//                    .getDbPrefix() + "shops MODIFY COLUMN itemConfig text CHARACTER SET utf8mb4 NOT NULL AFTER price", checkTask));
//            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
//                    .getDbPrefix() + "shops TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
//            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
//                    .getDbPrefix() + "messages TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
//            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
//                    .getDbPrefix() + "history TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
//        }
        plugin.getLogger().info("Finished!");
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
        if (shop.getLocation().getWorld() != null)
            worldName = shop.getLocation().getWorld().getName();
        manager.createReplace(plugin.getDbPrefix() + "shops")
                .setColumnNames("owner", "price", "itemConfig", "x", "y", "z", "world", "unlimited", "type", "extra")
                .setParams(ShopModerator.serialize(shop.getModerator()), shop.getPrice(), Util.serialize(shop.getItem()),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        worldName,
                        shop.isUnlimited() ? 1 : 0,
                        shop.getShopType().toID(),
                        shop.saveExtraToYaml()
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
                           @Nullable String currency, boolean disableDisplay, @Nullable String taxAccount) {
        manager.createUpdate(plugin.getDbPrefix() + "shops")
                .setColumnValues("owner", owner)
                .setColumnValues("itemConfig", Util.serialize(item))
                .setColumnValues("unlimited", unlimited)
                .setColumnValues("type", shopType)
                .setColumnValues("price", price)
                .setColumnValues("extra", extra)
                .setColumnValues("disableDisplay", disableDisplay ? 1 : 0)
                .setColumnValues("taxAccount", taxAccount)
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
    public void insertHistoryRecord(Object rec) {
        manager.createInsert(plugin.getDbPrefix() + "logs")
                .setColumnNames("time", "classname", "data")
                .setParams(System.currentTimeMillis(), rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
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
