/*
 * This file is a part of project QuickShop, the name is DatabaseManager.java
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

import lombok.Getter;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Timer;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.WarningSender;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.sql.*;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Queued database manager. Use queue to solve run SQL make server lagg issue.
 */
public class DatabaseManager implements Reloadable {

    private final Queue<DatabaseTask> sqlQueue = new LinkedBlockingQueue<>();

    @NotNull
    @Getter
    private final AbstractDatabaseCore database;

    @NotNull
    private final QuickShop plugin;

    @NotNull
    private final WarningSender warningSender;
    private boolean useQueue;
    @Nullable
    private BukkitTask task;

    /**
     * Queued database manager. Use queue to solve run SQL make server lagg issue.
     *
     * @param plugin plugin main class
     * @param dbCore database core
     * @throws ConnectionException when database connection failed
     */
    public DatabaseManager(@NotNull QuickShop plugin, @NotNull AbstractDatabaseCore dbCore) throws ConnectionException {
        this.plugin = plugin;
        this.warningSender = new WarningSender(plugin, 600000);
        this.database = dbCore;
        plugin.getReloadManager().register(this);
        init();

    }

    private void init() throws ConnectionException {
        this.useQueue = plugin.getConfig().getBoolean("database.queue");
        if (task != null) {
            task.cancel();
            plugin.getDatabaseManager().runTask();
        }
        DatabaseConnection connection = database.getConnection();
        try {
            if (!connection.isValid()) {
                throw new DatabaseManager.ConnectionException("The database does not appear to be valid!");
            }
        } finally {
            connection.release();
        }
        if (useQueue) {
            try {
                task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                    if (!task.isCancelled()) {
                        plugin.getDatabaseManager().runTask();
                    }
                }, 1, plugin.getConfig().getLong("database.queue-commit-interval") * 20);
            } catch (IllegalPluginAccessException e) {
                Util.debugLog("Plugin is disabled but trying create database task, move to Main Thread...");
                plugin.getDatabaseManager().runTask();
            }
        }
    }

    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    boolean hasTable(@NotNull String table) throws SQLException {
        DatabaseConnection connection = database.getConnection();
        boolean match = false;
        try (ResultSet rs = connection.get().getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    match = true;
                    break;
                }
            }
        } finally {
            connection.release();
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

        DatabaseConnection connection = database.getConnection();
        String query = "SELECT * FROM " + table + " LIMIT 1";
        boolean match = false;
        try (PreparedStatement ps = connection.get().prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnLabel(i).equals(column)) {
                    match = true;
                    break;
                }
            }
        } catch (SQLException e) {
            return match;
        } finally {
            connection.release();
        }
        return match; // Uh, wtf.
    }

    /**
     * Internal method, runTasks in queue.
     */
    private synchronized void runTask() { // synchronized for QUICKSHOP-WX
        if (sqlQueue.isEmpty()) {
            return;
        }
        DatabaseConnection dbconnection = this.database.getConnection();
        //We do not close the connection since is reusable
        Connection connection = dbconnection.get();
        try {
            //start our commit
            connection.setAutoCommit(false);
            Timer ctimer = new Timer(true);
            while (true) {
                if (!dbconnection.isValid()) {
                    warningSender.sendWarn("Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).");
                    return; // Waiting next crycle and hope it success reconnected.
                }

                Timer timer = new Timer(true);
                DatabaseTask task = sqlQueue.poll();
                if (task == null) {
                    break;
                }

                task.run(connection);
                long tookTime = timer.stopAndGetTimePassed();
                if (tookTime > 300) {
                    warningSender.sendWarn(
                            "Database performance warning: It took too long time ("
                                    + tookTime
                                    + "ms) to execute the task, it may cause the network connection with MySQL server or just MySQL server too slow, change to a better MySQL server or switch to a local SQLite database!");
                }
            }
            if (!connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
            }
            long tookTime = ctimer.stopAndGetTimePassed();
            if (tookTime > 5500) {
                warningSender.sendWarn(
                        "Database performance warning: It took too long time ("
                                + tookTime
                                + "ms) to execute the task, it may cause the network connection with MySQL server or just MySQL server too slow, change to a better MySQL server or switch to a local SQLite database!");
            }

        } catch (SQLException sqle) {
            plugin.getSentryErrorReporter().ignoreThrow();
            this.plugin
                    .getLogger()
                    .log(Level.WARNING, "Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).", sqle);
        } finally {
            dbconnection.release();
        }

//        try {
//            this.database.getConnection().commit();
//        } catch (SQLException e) {
//            try {
//                this.database.getConnection().rollback();
//            } catch (SQLException ignored) {
//            }
//        }
    }

    /**
     * Add DatabaseTask to queue waiting flush to database,
     *
     * @param task The DatabaseTask you want add in queue.
     */
    public void runInstantTask(DatabaseTask task) {
        DatabaseConnection connection = database.getConnection();
        task.run(connection.get());
        connection.release();
    }

    /**
     * Add DatabaseTask to queue waiting flush to database,
     *
     * @param task The DatabaseTask you want add in queue.
     */
    public void addDelayTask(DatabaseTask task) {
        if (useQueue) {
            sqlQueue.offer(task);
        } else {
            runInstantTask(task);
        }
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public synchronized void unInit() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        plugin.getLogger().info("Please wait for the data to flush its data...");
        runTask();
        database.close();
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
     * Represents a connection error, generally when the server can't connect to MySQL or something.
     */
    public static final class ConnectionException extends Exception {
        private static final long serialVersionUID = 8348749992936357317L;

        private ConnectionException(String msg) {
            super(msg);
        }

    }
}