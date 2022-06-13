/*
 *  This file is a part of project QuickShop, the name is DatabaseHelper.java
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

package com.ghostchu.quickshop.api.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Processing and handle most things about database ;)
 */
public interface DatabaseHelper {
    /**
     * Async gets the player last use locale code from database
     *
     * @param uuid     The player UUID
     * @param callback The callback
     */
    void getPlayerLocale(@NotNull UUID uuid, @NotNull Consumer<Optional<String>> callback);

    /**
     * Sets the player locale code to database
     *
     * @param uuid   The player UUID
     * @param locale The locale code
     */
    void setPlayerLocale(@NotNull UUID uuid, @NotNull String locale);

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
     * @param shop The shop object
     */
    long createData(@NotNull Shop shop) throws SQLException;

    @Nullable
    DataRecord getDataRecord(long dataId) throws SQLException;

    long createShop(long dataId) throws SQLException;

    void createShopMap(long shopId, @NotNull Location location) throws SQLException;

    /**
     * Remove a shop data mapping record from database
     *
     * @param world Shop world
     * @param x     Shop X
     * @param y     Shop Y
     * @param z     Shop Z
     */
    void removeShopMap(@NotNull String world, int x, int y, int z) throws SQLException;

    void removeShop(long shopId);

    void removeData(long dataId);

    long locateShopId(@NotNull String world, int x, int y, int z) throws SQLException;

    long locateShopDataId(long shopId) throws SQLException;

    /**
     * Select all messages that saved in the database
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    @NotNull
    SQLQuery selectAllMessages() throws SQLException;

    /**
     * Select specific table content
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    @NotNull
    SQLQuery selectTable(@NotNull String table) throws SQLException;

    /**
     * Select all shops that saved in the database
     *
     * @return Query result set
     * @throws SQLException Any errors related to SQL Errors
     */
    @NotNull
    SQLQuery selectAllShops() throws SQLException;

    /**
     * Create a transaction message record and save into database
     *
     * @param player  Target player
     * @param message The message content
     * @param time    System time
     */
    void saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time);

    void updateExternalInventoryProfileCache(long shopId, int space, int stock);

    void updateShop(@NotNull Shop shop) throws SQLException;

    /**
     * Insert a history record into logs table
     *
     * @param rec Record object that can be serialized by Gson.
     */
    void insertHistoryRecord(@NotNull Object rec);

    void insertMetricRecord(@NotNull ShopMetricRecord record);

    void insertTransactionRecord(@Nullable UUID from, @Nullable UUID to, double amount, @Nullable String currency, double taxAmount, @Nullable UUID taxAccount, @Nullable String error);
}
