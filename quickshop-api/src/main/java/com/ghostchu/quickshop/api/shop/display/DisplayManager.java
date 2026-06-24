package com.ghostchu.quickshop.api.shop.display;

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

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.display.DisplayManagerPutEvent;
import com.ghostchu.quickshop.api.event.display.DisplayManagerRemoveEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the display functionality for various display type entities.
 *
 * @param <T> The type of object or entity used for rendering displays.
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface DisplayManager<T> {

  /**
   * Retrieves a mapping between ShopChunk objects and their associated lists of display entities.
   *
   * This method provides access to the internal data structure used by the display manager to store and
   * organize displays according to their respective ShopChunk locations. The returned mapping allows
   * efficient retrieval, addition, or removal of display entities based on chunk-specific contexts.
   *
   * @return A {@code ConcurrentHashMap} where the keys represent {@code ShopChunk} objects,
   *         and the values are lists of display entities of type {@code T} associated with the respective chunks.
   */
  ConcurrentHashMap<ShopChunk, ConcurrentHashMap<Integer, T>> chunksMapping();

  /**
   * Loads the required resources, configurations, or internal states for this display manager.
   * This method prepares the manager for operation, ensuring it is in a ready state to handle
   * display-related actions, such as creating or managing displays.
   *
   * Typically invoked during the initialization process or when reloading the manager.
   */
  void load();

  /**
   * Unloads the resources, configurations, or internal states for this display manager.
   * This method is typically used to clean up or release resources when the display manager
   * is no longer needed or is being shut down.
   *
   * Invoking this method ensures that the manager releases any held resources properly
   * and avoids potential memory leaks or unintended behavior.
   */
  void unload();

  /**
   * Creates and returns a new display instance associated with the specified shop.
   *
   * @param shop The shop for which the display instance will be created. Must not be null.
   * @return The newly created display instance of type {@code T}.
   * @throws IllegalArgumentException If the provided shop is invalid or null.
   */
  T create(@NotNull final Shop shop);

  /**
   * Adds a display object to the display manager, associating it with a specified {@code ShopChunk}
   * and a unique location hash.
   *
   * This method creates a new {@code DisplayManagerPutEvent} to handle the insertion operation.
   * The display is then added to the internal mapping of chunks to displays for efficient management.
   *
   * @param key The {@code ShopChunk} representing the specific location for this display.
   *            Must not be null.
   * @param locationHash A unique integer hash that identifies the specific location within the chunk.
   * @param display The display object to associate with the specified {@code ShopChunk}.
   *                Must not be null.
   */
  default void put(@NotNull final ShopChunk key, final int locationHash, @NotNull final T display) {

    final DisplayManagerPutEvent<T> event = new DisplayManagerPutEvent<>(key, display);
    event.callEvent();

    chunksMapping().computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(locationHash, display);
  }

  /**
   * Removes the specified display object associated with the given ShopChunk key
   * from the manager. This operation detaches the display from the provided
   * ShopChunk context.
   *
   * @param key The ShopChunk key representing the specific chunk related to the shop.
   *            Must not be null.
   * @param locationHash A unique integer hash that identifies the specific location within the chunk.
   * @param display The display object to be removed from the manager. This represents
   *                the display entity or type being managed. Must not be null.
   */
  default void remove(@NotNull final ShopChunk key, final int locationHash, @NotNull final T display) {

    final DisplayManagerRemoveEvent<T> event = new DisplayManagerRemoveEvent<>(key, display);
    event.callEvent();

    chunksMapping().computeIfPresent(key, (mapKey, displays) -> {

      displays.remove(locationHash);

      return displays.isEmpty() ? null : displays;
    });
  }

  default boolean allowEnchants() {
    return QuickShopAPI.getInstance().getConfig().getBoolean("shop.display-allow-enchants", true);
  }

  default boolean useItemName() {
    return QuickShopAPI.getInstance().getConfig().getBoolean("shop.display-item-use-name");
  }
}