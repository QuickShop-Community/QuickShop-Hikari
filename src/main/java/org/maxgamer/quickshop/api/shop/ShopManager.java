/*
 * This file is a part of project QuickShop, the name is ShopManager.java
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

package org.maxgamer.quickshop.api.shop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The manager that managing shops
 */
@SuppressWarnings("ALL")
public interface ShopManager {

    /**
     * Checks other plugins to make sure they can use the chest they're making a shop.
     *
     * @param p  The player to check
     * @param b  The block to check
     * @param bf The blockface to check
     * @return True if they're allowed to place a shop there.
     */
    boolean canBuildShop(@NotNull Player p, @NotNull Block b, @NotNull BlockFace bf);

    /**
     * Returns a map of World - Chunk - Shop
     *
     * @return a map of World - Chunk - Shop
     */
    @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops();

    /**
     * Returns a new shop iterator object, allowing iteration over shops easily, instead of sorting
     * through a 3D map.
     *
     * @return a new shop iterator object.
     */
    @NotNull Iterator<Shop> getShopIterator();

    /**
     * Removes all shops from memory and the world. Does not delete them from the database. Call
     * this on plugin disable ONLY.
     */
    void clear();

    /**
     * Returns a map of Shops
     *
     * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are
     *          used.
     * @return Shops
     */
    @Nullable Map<Location, Shop> getShops(@NotNull Chunk c);

    @Nullable Map<Location, Shop> getShops(@NotNull String world, int chunkX, int chunkZ);

    /**
     * Returns a map of Chunk - Shop
     *
     * @param world The name of the world (case sensitive) to get the list of shops from
     * @return a map of Chunk - Shop
     */
    @Nullable Map<ShopChunk, Map<Location, Shop>> getShops(@NotNull String world);

    /**
     * Create a shop use Shop and Info object.
     *
     * @param shop The shop object
     * @param info The info object
     */
    void createShop(@NotNull Shop shop, @NotNull Info info);

    /**
     * Format the price use economy system
     *
     * @param d        price
     * @param currency currency
     * @param world    shop world
     * @return formated price
     */
    @Nullable String format(double d, @NotNull World world, @Nullable String currency);

    /**
     * Format the price use economy system
     *
     * @param d    price
     * @param shop The shop
     * @return formated price
     */
    @Nullable String format(double d, @NotNull Shop shop);

    /**
     * Gets a shop in a specific location
     * ATTENTION: This not include attached shops (double-chest)
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Nullable Shop getShop(@NotNull Location loc);

    /**
     * Gets a shop in a specific location
     * ATTENTION: This not include attached shops (double-chest)
     *
     * @param loc                  The location to get the shop from
     * @param skipShopableChecking whether to check is shopable
     * @return The shop at that location
     */
    @Nullable Shop getShop(@NotNull Location loc, boolean skipShopableChecking);

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    @Nullable Shop getShopIncludeAttached(@Nullable Location loc);

    /**
     * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc      The location to get the shop from
     * @param useCache whether to use cache
     * @return The shop at that location
     */
    @Nullable Shop getShopIncludeAttached(@Nullable Location loc, boolean useCache);

    void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop);

    @Nullable Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId);

    @Nullable Shop getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId, boolean includeInvalid);

    void handleChat(@NotNull Player p, @NotNull String msg);

    //   void handleChat(@NotNull Player p, @NotNull String msg, boolean bypassProtectionChecks);

    /**
     * Load shop method for loading shop into mapping, so getShops method will can find it. It also
     * effects a lots of feature, make sure load it after create it.
     *
     * @param world The world the shop is in
     * @param shop  The shop to load
     */
    void loadShop(@NotNull String world, @NotNull Shop shop);

    /**
     * Adds a shop to the world. Does NOT require the chunk or world to be loaded Call shop.onLoad
     * by yourself
     *
     * @param world The name of the world
     * @param shop  The shop to add
     */
    void addShop(@NotNull String world, @NotNull Shop shop);

    /**
     * Removes a shop from the world. Does NOT remove it from the database. * REQUIRES * the world
     * to be loaded Call shop.onUnload by your self.
     *
     * @param shop The shop to remove
     */
    void removeShop(@NotNull Shop shop);

    /**
     * @return Returns the Map. Info contains what their last question etc was.
     */
    @NotNull Map<UUID, Info> getActions();

    /**
     * Get all loaded shops.
     *
     * @return All loaded shops.
     */
    @NotNull Set<Shop> getLoadedShops();

    /**
     * Get a players all shops.
     *
     * <p>Make sure you have caching this, because this need a while to get player's all shops
     *
     * @param playerUUID The player's uuid.
     * @return The list have this player's all shops.
     */
    @NotNull List<Shop> getPlayerAllShops(@NotNull UUID playerUUID);

    /**
     * Returns all shops in the whole database, include unloaded.
     *
     * <p>Make sure you have caching this, because this need a while to get all shops
     *
     * @return All shop in the database
     */
    @NotNull List<Shop> getAllShops();

    /**
     * Get the all shops in the world.
     *
     * @param world The world you want get the shops.
     * @return The list have this world all shops
     */
    @NotNull List<Shop> getShopsInWorld(@NotNull World world);

    @Deprecated
    double getTax(@NotNull Shop shop, @NotNull Player p);

    double getTax(@NotNull Shop shop, @NotNull UUID p);

    boolean shopIsNotValid(@NotNull UUID uuid, @NotNull Info info, @NotNull Shop shop);

    /**
     * Change the owner to unlimited shop owner.
     * It defined in configuration.
     */
    void migrateOwnerToUnlimitedShopOwner(Shop shop);

    /**
     * Getting the Shop Price Limiter
     *
     * @return The shop price limiter
     */
    @NotNull
    PriceLimiter getPriceLimiter();

    /**
     * Send a purchaseSuccess message for a player.
     *
     * @param purchaser Target player
     * @param shop      Target shop
     * @param amount    Trading item amounts.
     */
    @ApiStatus.Experimental
    void sendPurchaseSuccess(@NotNull UUID purchaser, @NotNull Shop shop, int amount);

    /**
     * Send a sellSuccess message for a player.
     *
     * @param seller Target player
     * @param shop   Target shop
     * @param amount Trading item amounts.
     */
    @ApiStatus.Experimental
    void sendSellSuccess(@NotNull UUID seller, @NotNull Shop shop, int amount);

    /**
     * Send a shop infomation to a player.
     *
     * @param p    Target player
     * @param shop The shop
     */
    @ApiStatus.Experimental
    void sendShopInfo(@NotNull Player p, @NotNull Shop shop);

}
