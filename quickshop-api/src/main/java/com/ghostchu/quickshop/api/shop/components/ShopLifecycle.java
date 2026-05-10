package com.ghostchu.quickshop.api.shop.components;

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

import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * ShopLifecycle
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ShopLifecycle {
  /**
   * Getting ConfigurationSection (extra data) instance of your plugin namespace)
   *
   * @param plugin The plugin and plugin name will used for namespace
   *
   * @return ExtraSection, save it through Shop#setExtra. If you don't save it, it may randomly lose
   * or save
   */
  @NotNull
  ConfigurationSection getExtra(@NotNull Plugin plugin);

  /**
   * Save the extra data to the shop.
   *
   * @param plugin Plugin instace
   * @param data   The data table
   */
  void setExtra(@NotNull Plugin plugin, @NotNull ConfigurationSection data);

  /**
   * Save the plugin extra data to Json format
   *
   * @return The json string
   */
  @NotNull
  String saveExtraToYaml();

  @NotNull
  DataRecord asDataRecord();

  /**
   * Getting ShopInfoStorage that you can use for storage the shop data
   *
   * @return ShopInfoStorage
   */
  ShopInfoStorage asInfoStorage();

  /**
   * Gets the symbol link that created by InventoryWrapperManager
   *
   * @return InventoryWrapper
   */
  @NotNull
  String asSymbolLink();

  /**
   * Gets if shop is dirty (so shop will be save)
   *
   * @return Is dirty
   */
  boolean isDirty();

  /**
   * Sets dirty status
   *
   * @param isDirty Shop is dirty
   */
  void setDirty(boolean isDirty);

  /**
   * Sets shop is dirty
   */
  void markDirty();

  /**
   * Get this container shop is loaded or unloaded.
   *
   * @return Loaded
   */
  boolean isLoaded();

  /**
   * Checks whether the shop is marked as deleted.
   *
   * @return {@code true} if the shop is deleted, {@code false} otherwise
   */
  boolean isDeleted();

  @Deprecated()
  @ApiStatus.Internal
  void handleLoading();

  @Deprecated()
  @ApiStatus.Internal
  void handleUnloading(boolean dontTouchWorld);

  /**
   * Update shop data to database
   */
  @NotNull
  CompletableFuture<Void> update();

  /**
   * Update shop data to database synchronously. This will create the completeable future for the save
   * function, and wait for it to complete. DON'T USE IF YOU DON'T KNOW WHAT YOU'RE DOING!
   *
   * @throws RuntimeException
   */
  void updateSync() throws RuntimeException;
}