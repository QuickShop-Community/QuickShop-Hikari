/*
 * This file is a part of project QuickShop, the name is DatabaseHelper.java
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

package org.maxgamer.quickshop.api.database;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.database.DataType;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public interface DatabaseHelper {
    /**
     * Creates a column with specific name and data type
     *
     * @param tableName  The table name
     * @param columnName The column name
     * @param type       The data type
     * @return Create result
     */
    boolean createColumn(@NotNull String tableName, @NotNull String columnName, @NotNull DataType type);

    /**
     * Cleanup transaction messages that saved in database
     *
     * @param weekAgo How many weeks ago messages should we clean up
     */
    void cleanMessage(long weekAgo);

    /**
     * Purge and clean all saved transaction message in data that should send to specific player
     *
     * @param player The player
     */
    void cleanMessageForPlayer(@NotNull UUID player);

    /**
     * Create a shop data record sand save into database
     *
     * @param shop      The shop object
     * @param onSuccess Success callback
     * @param onFailed  Fails callback
     */
    void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed);

    /**
     * Remove a shop data record from database
     *
     * @param shop The shop
     */
    void removeShop(Shop shop);

    /**
     * Remove a shop data record from database
     *
     * @param world Shop world
     * @param x     Shop X
     * @param y     Shop Y
     * @param z     Shop Z
     */
    void removeShop(String world, int x, int y, int z);

    /**
     * Select all messages that saved in the database
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    WarpedResultSet selectAllMessages() throws SQLException;

    /**
     * Select specific table content
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    WarpedResultSet selectTable(String table) throws SQLException;

    /**
     * Select all shops that saved in the database
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    WarpedResultSet selectAllShops() throws SQLException;

    /**
     * Create a transaction message record and save into database
     *
     * @param player  Target player
     * @param message The message content
     * @param time    System time
     */
    void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time);


    /**
     * Upgrade legacy name based data record to uniqueId based record
     *
     * @param ownerUUID The owner unique id
     * @param x         Shop X
     * @param y         Shop Y
     * @param z         Shop Z
     * @param worldName Shop World
     */
    void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName);

    /**
     * Update external cache data
     * (Used for Web UI or other something like that)
     *
     * @param shop  The shop
     * @param space The shop remaining space
     * @param stock The shop remaining stock
     */
    void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock);

    /**
     * Update a shop data into the database
     *
     * @param owner          Shop owner
     * @param item           Shop item
     * @param unlimited      Shop unlimited
     * @param shopType       Shop type
     * @param price          Shop price
     * @param x              Shop x
     * @param y              Shop y
     * @param z              Shop z
     * @param world          Shop world
     * @param extra          Shop extra data
     * @param currency       Shop currency
     * @param disableDisplay Shop display disabled status
     * @param taxAccount     Shop specific tax account
     */
    void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                    double price, int x, int y, int z, @NotNull String world, @NotNull String extra,
                    @Nullable String currency, boolean disableDisplay, @Nullable String taxAccount,
                    @NotNull String inventorySymbolLink, @NotNull String inventoryWrapperName);

    /**
     * Insert a history record into logs table
     *
     * @param rec Record object that can be serialized by Gson.
     */
    void insertHistoryRecord(Object rec);

}
