package com.ghostchu.quickshop.api.database;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.database.bean.ShopRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Processing and handle most things about database ;)
 */
public interface DatabaseHelper {
    /**
     * Async gets the player last use locale code from database
     *
     * @param uuid The player UUID
     */
    CompletableFuture<@Nullable String> getPlayerLocale(@NotNull UUID uuid);

    /**
     * Sets the player locale code to database
     *
     * @param uuid   The player UUID
     * @param locale The locale code
     * @return
     */
    CompletableFuture<Integer> setPlayerLocale(@NotNull UUID uuid, @NotNull String locale);

    /**
     * Cleanup transaction messages that saved in database
     *
     * @param weekAgo How many weeks ago messages should we clean up
     * @return
     */
    CompletableFuture<Integer> cleanMessage(long weekAgo);

    /**
     * Purge and clean all saved transaction message in data that should send to specific player
     *
     * @param player The player
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> cleanMessageForPlayer(@NotNull UUID player);

    /**
     * Create a shop data record sand save into database
     *
     * @param shop The shop object
     */
    @NotNull CompletableFuture<@NotNull Long> createData(@NotNull Shop shop);

    /**
     * Query and getting the data record by data Id
     *
     * @param dataId The data Id
     * @return The data record, null for not exists
     * @throws SQLException something going wrong
     */
    @NotNull CompletableFuture<@Nullable DataRecord> getDataRecord(long dataId);

    /**
     * Create a shop record in database
     *
     * @param dataId The data Id that shop id point to
     * @return The shop id
     * @throws SQLException something going wrong
     */
    CompletableFuture<Long> createShop(long dataId);

    /**
     * Creates a shop mapping that mapping a location to specific shop record id
     *
     * @param shopId   The shop record id
     * @param location The shop location
     * @return
     * @throws SQLException something going wrong
     */
    CompletableFuture<@NotNull Integer> createShopMap(long shopId, @NotNull Location location);

    /**
     * Remove a shop data mapping record from database
     *
     * @param world Shop world
     * @param x     Shop X
     * @param y     Shop Y
     * @param z     Shop Z
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> removeShopMap(@NotNull String world, int x, int y, int z);

    /**
     * Remove a shop data record from database
     *
     * @param shopId The shop record id
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> removeShop(long shopId);

    /**
     * Remove a data record from database
     *
     * @param dataId The data record id
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> removeData(long dataId);

    /**
     * Locate a shop record from database by location
     *
     * @param world The shop world
     * @param x     The shop X
     * @param y     The shop Y
     * @param z     The shop Z
     * @return The shop record id
     */
    CompletableFuture<Long> locateShopId(@NotNull String world, int x, int y, int z);

    /**
     * Locate a shop record from database by shop record id
     *
     * @param shopId The shop record id
     * @return The shop record
     */
    @NotNull CompletableFuture<@Nullable Long> locateShopDataId(long shopId);

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

//    /**
//     * Select all shops that saved in the database
//     *
//     * @return Query result set
//     * @throws SQLException Any errors related to SQL Errors
//     */
//    @NotNull
//    List<DataRecord> selectAllShops() throws SQLException;

    /**
     * Create a transaction message record and save into database
     *
     * @param player  Target player
     * @param message The message content
     * @param time    System time
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> saveOfflineTransactionMessage(@NotNull UUID player, @NotNull String message, long time);

    /**
     * Update inventory data to external cache table
     *
     * @param shopId The shop record id
     * @param space  The inventory space
     * @param stock  The inventory stock
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> updateExternalInventoryProfileCache(long shopId, int space, int stock);

    /**
     * Update the shop profile to database
     *
     * @param shop The shop object
     * @return
     */
    CompletableFuture<Void> updateShop(@NotNull Shop shop);

    /**
     * Insert a history record into logs table
     *
     * @param rec Record object that can be serialized by Gson.
     * @return
     */
    @NotNull CompletableFuture<@NotNull Integer> insertHistoryRecord(@NotNull Object rec);

    @NotNull CompletableFuture<@NotNull Integer> insertMetricRecord(@NotNull ShopMetricRecord record);

    void insertTransactionRecord(@Nullable UUID from, @Nullable UUID to, double amount, @Nullable String currency, double taxAmount, @Nullable UUID taxAccount, @Nullable String error);

    @NotNull List<ShopRecord> listShops(boolean deleteIfCorrupt);
}
