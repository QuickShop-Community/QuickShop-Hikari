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
   * Updates the shop data to the database synchronously. This method blocks the thread
   * until the update operation completes.
   * <strong>IMPORTANT:</strong> Use this method only if you are certain of its implications,
   * as it may cause performance issues or deadlocks when used incorrectly in asynchronous
   * or multi-threaded environments.
   *
   * @throws RuntimeException if an error occurs during the update process
   */
  void updateSync() throws RuntimeException;
}