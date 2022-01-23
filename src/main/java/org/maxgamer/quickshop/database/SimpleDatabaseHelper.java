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
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A Util to execute all SQLs.
 */
public class SimpleDatabaseHelper implements DatabaseHelper, Reloadable {


    @NotNull
    private final DatabaseManager manager;

    @NotNull
    private final QuickShop plugin;

    public SimpleDatabaseHelper(@NotNull QuickShop plugin, @NotNull DatabaseManager manager) throws SQLException {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getReloadManager().register(this);
        init();
    }

    private void init() throws SQLException {
        if (!manager.hasTable(plugin.getDbPrefix() + "shops")) {
            createShopsTable();
        }
        if (!manager.hasTable(plugin.getDbPrefix() + "messages")) {
            createMessagesTable();
        }
        if (!manager.hasTable(plugin.getDbPrefix() + "logs")) {
            createLogsTable();
        }
        if (!manager.hasTable(plugin.getDbPrefix() + "external_cache")) {
            createExternalCacheTable();
        }
        checkColumns();
    }

    /**
     * Creates the database table 'shops'.
     */

    private void createShopsTable() {
        String sqlString = "CREATE TABLE " + plugin.getDbPrefix() + "shops (owner  VARCHAR(255) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARSET utf8 NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
        if (manager.getDatabase() instanceof MySQLCore) {
            sqlString = "CREATE TABLE " + plugin.getDbPrefix() + "shops (owner  VARCHAR(255) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
        }
        manager.runInstantTask(new DatabaseTask(sqlString));
    }

    /**
     * Creates the database table 'messages'
     */
    private void createMessagesTable() {
        String createTable = "CREATE TABLE " + plugin.getDbPrefix()
                + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) NOT NULL, time  BIGINT(32) NOT NULL );";
        if (manager.getDatabase() instanceof MySQLCore) {
            createTable = "CREATE TABLE " + plugin.getDbPrefix()
                    + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL , time  BIGINT(32) NOT NULL );";
        }
        manager.runInstantTask(new DatabaseTask(createTable));
    }

    private void createLogsTable() {
        String createTable = "CREATE TABLE " + plugin.getDbPrefix()
                + "logs (time BIGINT(32) NOT NULL);";
        manager.runInstantTask(new DatabaseTask(createTable));
        createColumn("logs", "classname", new DataType(DataTypeMapping.TEXT, null, ""));
        createColumn("logs", "data", new DataType(DataTypeMapping.LONGTEXT, null, ""));
    }

    private void createExternalCacheTable() {
        String createTable = "CREATE TABLE " + plugin.getDbPrefix()
                + "external_cache  (x INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, PRIMARY KEY (x, y, z, world));";
        manager.runInstantTask(new DatabaseTask(createTable));
        createColumn("external_cache", "space", new DataType(DataTypeMapping.INT, null));
        createColumn("external_cache", "stock", new DataType(DataTypeMapping.INT, null));
    }


    /**
     * Verifies that all required columns exist.
     */
    private void checkColumns() {
        plugin.getLogger().info("Checking and updating database columns, it may take a while...");
        DatabaseTask.Task checkTask = new DatabaseTask.Task() {
            @Override
            public void edit(PreparedStatement ps) {

            }

            @Override
            public void onFailed(SQLException e) {

            }
        };
        // V3.4.2
        manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                .getDbPrefix() + "shops MODIFY COLUMN price double(32,2) NOT NULL AFTER owner", checkTask));
        // V3.4.3
        manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                .getDbPrefix() + "messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message", checkTask));
        //Extra column
        createColumn("shops", "extra", new DataType(DataTypeMapping.LONGTEXT, null, ""));
        createColumn("shops", "currency", new DataType(DataTypeMapping.TEXT));
        createColumn("shops", "disableDisplay", new DataType(DataTypeMapping.INT, null, -1));
        createColumn("shops", "taxAccount", new DataType(DataTypeMapping.VARCHAR, 255));
        createColumn("shops", "inventorywrapper", new DataType(DataTypeMapping.VARCHAR, 255));
        createColumn("shops", "symbollink", new DataType(DataTypeMapping.LONGTEXT,null));
        if (manager.getDatabase() instanceof MySQLCore) {
            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                    .getDbPrefix() + "messages MODIFY COLUMN message text CHARACTER SET utf8mb4 NOT NULL AFTER owner", checkTask));
            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                    .getDbPrefix() + "shops MODIFY COLUMN itemConfig text CHARACTER SET utf8mb4 NOT NULL AFTER price", checkTask));
            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                    .getDbPrefix() + "shops TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                    .getDbPrefix() + "messages TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
            manager.runInstantTask(new DatabaseTask("ALTER TABLE " + plugin
                    .getDbPrefix() + "history TO CHARACTER SET uft8mb4 COLLATE utf8mb4_general_ci", checkTask));
        }
        plugin.getLogger().info("Finished!");
    }

    @Override
    public boolean createColumn(@NotNull String tableName, @NotNull String columnName, @NotNull DataType type) {

        try {
            String table = plugin.getDbPrefix() + tableName;
            if (manager.hasColumn(table, columnName)) {
                return false;
            }
            String sqlString;
            if (manager.getDatabase() instanceof MySQLCore) {
                sqlString = "alter table " + table + " add " + columnName + " " + type.getDatatype().getMysql();
            } else {
                sqlString = "alter table " + table + " add column " + columnName + " " + type.getDatatype().getSqlite();
            }
            if (type.getLength() != null) {
                sqlString += "(" + type.getLength().toString() + ") ";
            }
            Util.debugLog("Append sql for creating column is " + sqlString);
            manager.runInstantTask(new DatabaseTask(sqlString, new DatabaseTask.Task() {
                @Override
                public void edit(PreparedStatement ps) {
                }

                @Override
                public void onFailed(SQLException e) {
                    QuickShop.getInstance().getLogger().log(Level.WARNING, "Cannot create column " + columnName, e);
                    Util.debugLog("Cannot create column " + columnName + ", caused by:" + e.getMessage());
                }
            }));
            return true;
        } catch (SQLException sqlException) {
            QuickShop.getInstance().getLogger().log(Level.WARNING, "Cannot create column " + columnName, sqlException);
            Util.debugLog("Cannot create column " + columnName + " casued by:" + sqlException.getMessage());
            return false;
        }
    }

    @Override
    public void cleanMessage(long weekAgo) {
        String sqlString = "DELETE FROM " + plugin
                .getDbPrefix() + "messages WHERE time < ?";
        manager.addDelayTask(new DatabaseTask(sqlString, ps -> ps.setLong(1, weekAgo)));
    }

    @Override
    public void cleanMessageForPlayer(@NotNull UUID player) {
        String sqlString = "DELETE FROM " + plugin.getDbPrefix() + "messages WHERE owner = ?";
        manager.addDelayTask(new DatabaseTask(sqlString, (ps) -> ps.setString(1, player.toString())));
    }

    @Override
    public void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed) {
        removeShop(shop); //First purge old exist shop before create new shop.
        String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "shops (owner, price, itemConfig, x, y, z, world, unlimited, type, extra, inventorywrapper, symbollink) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        manager.addDelayTask(new DatabaseTask(sqlString, new DatabaseTask.Task() {
            @Override
            public void edit(PreparedStatement ps) throws SQLException {
                Location location = shop.getLocation();
                //plugin.getDB().execute(q, owner, price, Util.serialize(item), x, y, z, world, unlimited, shopType);
                ps.setString(1, ShopModerator.serialize(shop.getModerator()));
                ps.setDouble(2, shop.getPrice());
                ps.setString(3, Util.serialize(shop.getItem()));
                ps.setInt(4, location.getBlockX());
                ps.setInt(5, location.getBlockY());
                ps.setInt(6, location.getBlockZ());
                String worldName = "undefined";
                if (location.isWorldLoaded()) {
                    worldName = location.getWorld().getName();
                } else {
                    plugin.getLogger().warning("Warning: Shop " + shop + " had null world name due we will save it as undefined world to trying keep data.");
                }
                ps.setString(7, worldName);
                ps.setInt(8, shop.isUnlimited() ? 1 : 0);
                ps.setInt(9, shop.getShopType().toID());
                ps.setString(10, shop.saveExtraToYaml());
                ps.setString(11, shop.getInventoryWrapperProvider());
                ps.setString(12, shop.saveToSymbolLink());
            }

            @Override
            public void onSuccess() {
                if (!shop.isDeleted() && onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailed(SQLException e) {
                if (onFailed != null) {
                    onFailed.accept(e);
                } else {
                    plugin.getLogger().log(Level.WARNING, "Warning: Shop " + shop + " failed save to database, the shop may disappear after plugin reload or server restart!", e);
                }
            }
        }));
    }

    @Override
    public void removeShop(Shop shop) {
        String sqlString = "DELETE FROM "
                + plugin.getDbPrefix()
                + "shops WHERE x = ? AND y = ? AND z = ? AND world = ?"
                + (manager.getDatabase() instanceof MySQLCore ? " LIMIT 1" : "");
        manager.addDelayTask(new DatabaseTask(sqlString, (ps) -> {
            Location location = shop.getLocation();
            ps.setInt(1, location.getBlockX());
            ps.setInt(2, location.getBlockY());
            ps.setInt(3, location.getBlockZ());
            ps.setString(4, location.getWorld().getName());
        }));

    }

    @Override
    public void removeShop(String world, int x, int y, int z) {
        String sqlString = "DELETE FROM "
                + plugin.getDbPrefix()
                + "shops WHERE x = ? AND y = ? AND z = ? AND world = ?"
                + (manager.getDatabase() instanceof MySQLCore ? " LIMIT 1" : "");
        manager.addDelayTask(new DatabaseTask(sqlString, (ps) -> {
            ps.setInt(1, x);
            ps.setInt(2, y);
            ps.setInt(3, z);
            ps.setString(4, world);
        }));

    }

    @Override
    public SimpleWarpedResultSet selectAllMessages() throws SQLException {
        return selectTable("messages");
    }

    @Override
    public SimpleWarpedResultSet selectTable(String table) throws SQLException {
        DatabaseConnection databaseConnection = manager.getDatabase().getConnection();
        Statement st = databaseConnection.get().createStatement();
        String sql = "SELECT * FROM " + plugin.getDbPrefix() + table;
        ResultSet resultSet = st.executeQuery(sql);
        //Resource closes will complete in this class
        return new SimpleWarpedResultSet(st, resultSet, databaseConnection);
    }

    @Override
    public SimpleWarpedResultSet selectAllShops() throws SQLException {
        return selectTable("shops");
    }

    @Override
    public void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time) {
        String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "messages (owner, message, time) VALUES (?, ?, ?)";
        manager.addDelayTask(
                new DatabaseTask(
                        sqlString,
                        (ps) -> {
                            ps.setString(1, player.toString());
                            ps.setString(2, message);
                            ps.setLong(3, time);
                        }));
    }

    @Override
    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
        String sqlString = "UPDATE " + plugin
                .getDbPrefix() + "shops SET owner = ? WHERE x = ? AND y = ? AND z = ? AND world = ?" + (
                manager.getDatabase() instanceof MySQLCore ? " LIMIT 1" : "");
        manager.addDelayTask(
                new DatabaseTask(sqlString, ps -> {
                    ps.setString(1, ownerUUID);
                    ps.setInt(2, x);
                    ps.setInt(3, y);
                    ps.setInt(4, z);
                    ps.setString(5, worldName);
                }));
    }

    @Override
    public void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock) {

        if (manager.getDatabase() instanceof MySQLCore) {
            String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "external_cache (x,y,z,world,space,stock) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE space = ?, stock = ?";
            manager.addDelayTask(
                    new DatabaseTask(sqlString, ps -> {
                        ps.setInt(1, shop.getLocation().getBlockX());
                        ps.setInt(2, shop.getLocation().getBlockY());
                        ps.setInt(3, shop.getLocation().getBlockZ());
                        ps.setString(4, shop.getLocation().getWorld().getName());
                        ps.setInt(5, space);
                        ps.setInt(6, stock);
                        ps.setInt(7, space);
                        ps.setInt(8, stock);
                    }));
        } else {
            String createString = "INSERT OR IGNORE INTO " + plugin.getDbPrefix() + "external_cache (x,y,z,world,space,stock) VALUES (?,?,?,?,?,?)";
            manager.addDelayTask(
                    new DatabaseTask(createString, ps -> {
                        ps.setInt(1, shop.getLocation().getBlockX());
                        ps.setInt(2, shop.getLocation().getBlockY());
                        ps.setInt(3, shop.getLocation().getBlockZ());
                        ps.setString(4, shop.getLocation().getWorld().getName());
                        ps.setInt(5, space);
                        ps.setInt(6, stock);
                    }));
            String updateString = "UPDATE " + plugin.getDbPrefix() + "external_cache SET space = ?, stock = ? WHERE x = ? AND y = ? AND z = ? AND world =?";
            manager.addDelayTask(
                    new DatabaseTask(updateString, ps -> {
                        ps.setInt(1, space);
                        ps.setInt(2, stock);
                        ps.setInt(3, shop.getLocation().getBlockX());
                        ps.setInt(4, shop.getLocation().getBlockY());
                        ps.setInt(5, shop.getLocation().getBlockZ());
                        ps.setString(6, shop.getLocation().getWorld().getName());
                    }));
        }

    }

    @Override
    public void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                           double price, int x, int y, int z, @NotNull String world, @NotNull String extra,
                           @Nullable String currency, boolean disableDisplay, @Nullable String taxAccount,
                           @NotNull String inventoryWrapperProvider, @NotNull String inventorySymbolLink) {
        String sqlString = "UPDATE " + plugin
                .getDbPrefix() + "shops SET owner = ?, itemConfig = ?, unlimited = ?, type = ?, price = ?," +
                " extra = ?, currency = ?, disableDisplay = ?, taxAccount = ?, inventorywrapper = ?, symbollink = ?" +
                " WHERE x = ? AND y = ? and z = ? and world = ?";
        Util.debugLog("CREATE->DB: ");
        manager.addDelayTask(new DatabaseTask(sqlString, ps -> {
            ps.setString(1, owner);
            ps.setString(2, Util.serialize(item));
            ps.setInt(3, unlimited);
            ps.setInt(4, shopType);
            ps.setDouble(5, price);
            ps.setString(6, extra);
            ps.setString(7, currency);
            ps.setInt(8, disableDisplay ? 1 : 0);
            ps.setString(9, taxAccount);
            ps.setString(10, inventoryWrapperProvider);
            ps.setString(11, inventorySymbolLink);
            ps.setInt(12, x);
            ps.setInt(13, y);
            ps.setInt(14, z);
            ps.setString(15, world);
        }));
        //db.execute(q, owner, Util.serialize(item), unlimited, shopType, price, x, y, z, world);

    }

    @Override
    public void insertHistoryRecord(Object rec) {
        String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "logs (time, classname, data) VALUES (?, ?, ?)";
        manager.addDelayTask(
                new DatabaseTask(
                        sqlString,
                        (ps) -> {
                            ps.setLong(1, System.currentTimeMillis());
                            ps.setString(2, rec.getClass().getName());
                            ps.setString(3, JsonUtil.getGson().toJson(rec));
                        }));
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
}
