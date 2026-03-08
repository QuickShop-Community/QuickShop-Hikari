package com.ghostchu.quickshop.api.shop.tag;

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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a service for managing shop tags.
 *
 * <p>This service is responsible for:</p>
 * <ul>
 *   <li>Normalizing and displaying tags</li>
 *   <li>Persisting tags to the database</li>
 *   <li>Removing tags from shops</li>
 *   <li>Toggling system-defined tags</li>
 * </ul>
 *
 * <p>Implementations are expected to connect directly to the database layer,
 * while a tag manager handles in-memory indexing and caching.</p>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface TagService {

  /**
   * Represents the index key used for total counts in tag count maps.
   *
   * @since 6.3.0.0
   */
  long TOTAL_INDEX = -1L;

  /**
   * Represents the reserved system tag for favorites.
   *
   * @since 6.3.0.0
   */
  String SYS_FAV = "@fav";

  /**
   * Represents the reserved system tag for watch tracking.
   *
   * @since 6.3.0.0
   */
  String SYS_WATCH = "@watch";

  /**
   * Represents the reserved system tag for avoided shops.
   *
   * @since 6.3.0.0
   */
  String SYS_AVOID = "@avoid";

  /**
   * Formats a stored tag for display to a player.
   *
   * <p>System tags are converted into friendly display names, while
   * custom tags are prefixed with a hash character.</p>
   *
   * @param sender the player receiving the display value
   * @param stored the stored tag value
   *
   * @return the formatted display value, or an empty string if the tag is null
   *
   * @since 6.3.0.0
   */
  String displayTag(Player sender, String stored);

  /**
   * Formats a stored tag for display while preserving system tags.
   *
   * <p>System tags beginning with {@code @} are returned unchanged,
   * while custom tags are prefixed with a hash character.</p>
   *
   * @param stored the stored tag value
   *
   * @return the formatted tag, or an empty string if the tag is null
   *
   * @since 6.3.0.0
   */
  String displayTagOrHash(String stored);

  /**
   * Normalizes a tag into its stored form.
   *
   * <p>This includes trimming whitespace, lowercasing the value,
   * removing a leading hash character, enforcing a maximum length, and validating allowed
   * characters.</p>
   *
   * @param input       the input tag
   * @param allowSystem whether reserved system tags are allowed
   *
   * @return the normalized tag, or {@code null} if invalid
   *
   * @since 6.3.0.0
   */
  @Nullable String normalizeTag(@Nullable String input, boolean allowSystem);

  /**
   * Adds a tag to a shop for a player.
   *
   * @param player the player applying the tag
   * @param shopId the shop id
   * @param tag    the tag to add
   *
   * @return a future containing {@code true} if the tag was added
   *
   * @since 6.3.0.0
   */
  CompletableFuture<Boolean> addShopTag(UUID player, long shopId, String tag);

  /**
   * Removes a tag from a shop for a player.
   *
   * @param player the player removing the tag
   * @param shopId the shop id
   * @param tag    the tag to remove
   *
   * @return a future containing {@code true} if the tag was removed
   *
   * @since 6.3.0.0
   */
  CompletableFuture<Boolean> removeShopTag(UUID player, long shopId, String tag);

  /**
   * Removes all tags from a shop.
   *
   * @param shopId the shop id
   *
   * @return a future containing {@code true} if tags were removed
   *
   * @since 6.3.0.0
   */
  CompletableFuture<Boolean> removeAllShopTags(long shopId);

  /**
   * Removes all tags applied by a specific player to a shop.
   *
   * @param shopId the shop id
   * @param player the player whose tags should be removed
   *
   * @return a future containing {@code true} if tags were removed
   *
   * @since 6.3.0.0
   */
  CompletableFuture<Boolean> removeAllShopTagsBy(long shopId, UUID player);

  /**
   * Toggles a reserved system tag on a shop for a player.
   *
   * <p>If the tag does not exist it will be added. If it already exists
   * it will be removed.</p>
   *
   * @param player the player toggling the tag
   * @param shopId the shop id
   * @param tag    the system tag
   *
   * @return a future containing {@code true} if the tag is enabled after the operation
   *
   * @since 6.3.0.0
   */
  CompletableFuture<Boolean> toggleSystemTag(UUID player, long shopId, String tag);
}