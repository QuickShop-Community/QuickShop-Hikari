package com.ghostchu.quickshop.api.shop;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;
import com.ghostchu.quickshop.api.shop.service.ShopActionResult;
import com.ghostchu.quickshop.api.shop.service.request.ShopCreateRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopDeleteRequest;
import com.ghostchu.quickshop.api.shop.service.request.ShopUpdateRequest;
import com.ghostchu.quickshop.api.shop.service.result.ShopCreateResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopDeleteResult;
import com.ghostchu.quickshop.api.shop.service.result.ShopUpdateResult;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ShopService
 *
 * T = Shop object
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopService<T extends ModernShop<?, ?, ?, ?>> {

  /**
   * Retrieves the ShopBuilderFactory instance associated with the ShopService implementation.
   *
   * @return ShopBuilderFactory instance responsible for providing builder objects for shop-related components,
   *         such as ShopItemBuilder, ShopMetaBuilder, ShopPermissionBuilder, and ShopPriceBuilder.
   */
  ShopBuilderFactory shopBuilderFactory();

  /**
   * Creates a new shop based on the provided creation request.
   *
   * @param request The request object containing all necessary information for shop creation,
   *                including the actor initiating the operation and the shop details.
   * @return A result object encapsulating details about the shop creation operation,
   *         including the success status, the created shop instance (if successful),
   *         and potential failure reasons if the operation failed.
   */
  @NotNull
  ShopActionResult<ShopCreateResult> createShop(@NotNull final ShopCreateRequest request);

  /**
   * Updates an existing shop based on the provided update request.
   *
   * @param request The request object containing the necessary details to update a shop,
   *                including the actor performing the operation and the shop to be updated.
   * @return A result object containing details about the update operation, including
   *         the success status, any changes made, and potential failure reasons if the
   *         operation was unsuccessful.
   */
  @NotNull
  ShopActionResult<ShopUpdateResult> updateShop(@NotNull final ShopUpdateRequest request);

  /**
   * Deletes a shop based on the provided delete request.
   *
   * @param request The request object containing the necessary details
   *                to delete a shop, such as the actor performing the operation
   *                and the target shop ID.
   * @return A result object containing information about the outcome of
   *         the delete operation, including success status and possible
   *         failure reasons.
   */
  @NotNull
  ShopActionResult<ShopDeleteResult> deleteShop(@NotNull final ShopDeleteRequest request);

  /**
   * Returns all shops in the whole database, include unloaded.
   *
   * <p>Make sure you have caching this, because this need a while to get all shops
   *
   * @return All shop in the database
   */
  @NotNull
  List<T> getAllShops();

  /**
   * Get all loaded shops.
   *
   * @return All loaded shops.
   */
  @NotNull
  Set<T> getLoadedShops();

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get player's all shops
   *
   * @param playerUUID The player's uuid.
   *
   * @return The list have this player's all shops.
   */
  @NotNull
  List<T> getAllShops(@NotNull QUser playerUUID);

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get player's all shops
   *
   * @param playerUUID The player's uuid.
   *
   * @return The list have this player's all shops.
   */
  @NotNull
  List<T> getAllShops(@NotNull UUID playerUUID);

  /**
   * Gets a shop by shop Id
   *
   * @return The shop object
   */
  @Nullable
  T getShop(long shopId);

  /**
   * Gets a shop in a specific location ATTENTION: This not include attached shops (double-chest)
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location
   */
  @Nullable
  T getShop(@NotNull Location loc);

  /**
   * Gets a shop in a specific location but via cache ATTENTION: This not include attached shops
   * (double-chest)
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location but via cache
   */
  @Nullable
  T getShopViaCache(@NotNull Location loc);

  /**
   * Gets a shop in a specific location ATTENTION: This not include attached shops (double-chest)
   *
   * @param loc                  The location to get the shop from
   * @param skipShopableChecking whether to check is shopable
   *
   * @return The shop at that location
   */
  @Nullable
  T getShop(@NotNull Location loc, boolean skipShopableChecking);


  @Nullable
  T getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId);

  @Nullable
  T getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId, boolean includeInvalid);

  /**
   * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop.
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location
   */
  @Nullable
  T getShopIncludeAttached(@Nullable Location loc);

  /**
   * Gets a shop in a specific location Include the attached shop, e.g DoubleChest shop. but via
   * cache
   *
   * @param loc The location to get the shop from
   *
   * @return The shop at that location but via cache
   */
  @Nullable
  T getShopIncludeAttachedViaCache(@Nullable Location loc);


  /**
   * Returns a new shop iterator object, allowing iteration over shops easily, instead of sorting
   * through a 3D map.
   *
   * @return a new shop iterator object.
   */
  @NotNull
  Iterator<T> getShopIterator();

  /**
   * Returns a map of World - Chunk - Shop
   *
   * @return a map of World - Chunk - Shop
   */
  @NotNull
  Map<String, Map<ShopChunk, Map<Location, T>>> getShops();

  /**
   * Returns a map of Shops
   *
   * @param c The chunk to search. Referencing doesn't matter, only coordinates and world are used.
   *
   * @return Shops
   */
  @NotNull
  Map<Location, T> getShops(@NotNull Chunk c);

  /**
   * Gets the shop at the world and specific chunk.
   *
   * @param world  The world to get the shop from
   * @param chunkX The chunk x coordinate
   * @param chunkZ The chunk z coordinate
   *
   * @return The shop at the world and specific chunk.
   */
  @NotNull
  Map<Location, T> getShops(@NotNull String world, int chunkX, int chunkZ);

  /**
   * Gets the shop at the world and specific chunk.
   *
   * @param shopChunk The shop chunk
   *
   * @return The shop at the world and specific chunk.
   */
  @NotNull
  Map<Location, T> getShops(@NotNull ShopChunk shopChunk);

  /**
   * Returns a map of Chunk - Shop
   *
   * @param world The name of the world (case sensitive) to get the list of shops from
   *
   * @return a map of Chunk - Shop
   */
  @NotNull
  Map<ShopChunk, Map<Location, T>> getShops(@NotNull String world);

  /**
   * Get the all shops in the world.
   *
   * @param world The world you want get the shops.
   *
   * @return The list have this world all shops
   */
  @NotNull
  List<T> getShopsInWorld(@NotNull World world);

  /**
   * Get the all shops in the world.
   *
   * @param worldName The world you want get the shops.
   *
   * @return The list have this world all shops
   */
  @NotNull
  List<T> getShopsInWorld(@NotNull String worldName);

  //Shop database operations.
  /**
   * Queries the database to retrieve the inventory cache for the specified shop.
   *
   * @param shop The shop instance for which the inventory cache is being queried. Must not be null.
   * @return A CompletableFuture that completes with the ShopInventoryCountCache containing
   *         inventory information for the given shop. The result is guaranteed to be non-null.
   */
  @NotNull
  CompletableFuture<@NotNull ShopInventoryCountCache> queryShopInventoryCacheInDatabase(@NotNull T shop);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param shop  The shop to load
   */
  void loadShop(@NotNull T shop);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param shop  The shop to load
   */
  void unloadShop(@NotNull T shop);

  /**
   * Load shop method for loading shop into mapping, so getShops method will can find it. It also
   * effects a lots of feature, make sure load it after create it.
   *
   * @param shop           The shop to load
   * @param chunkUnloading If unloadShop called caused by chunk unloading, when this is true,
   *                       QuickShop will try avoid any main-thread opreations to avoid
   *                       load-unload-load loop
   */
  void unloadShop(@NotNull T shop, boolean chunkUnloading);

  @Deprecated()
  ShopActionResult handleLoading();

  @Deprecated()
  ShopActionResult handleUnloading(boolean dontTouchWorld);

  /**
   * Registers a shop with the system and optionally persists the shop information.
   *
   * @param shop the shop object to be registered; must not be null
   * @param persist a flag indicating whether the shop should be persisted to storage
   * @return a CompletableFuture representing the asynchronous operation of registering the shop
   */
  CompletableFuture<?> registerShop(@NotNull T shop, boolean persist);

  /**
   * Unregisters the specified shop from the system. If persistence is enabled,
   * the removal will be reflected in the underlying storage to ensure the shop
   * is no longer persisted.
   *
   * @param shop    the shop instance to be unregistered; must not be null
   * @param persist indicates whether the unregister operation should be
   *                persisted in the storage
   * @return a CompletableFuture representing the asynchronous operation of
   *         unregistering the shop
   */
  CompletableFuture<?> unregisterShop(@NotNull T shop, boolean persist);
}