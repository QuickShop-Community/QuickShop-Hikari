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

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Provides an API for managing tags associated with shops and players.
 *
 * <p>The {@code TagManager} allows plugins and internal systems to:</p>
 * <ul>
 *   <li>Add, remove, or toggle tags on shops</li>
 *   <li>Query tags associated with a shop or player</li>
 *   <li>Filter shops based on tag criteria</li>
 *   <li>Clear tags globally, per shop, or per player</li>
 *   <li>Retrieve tag statistics and counts</li>
 * </ul>
 *
 * <p>Tags are stored per player per shop, meaning each player maintains
 * their own tagging view of shops. Tags may be used for systems such as
 * favorites, watchlists, or custom player categorization.</p>
 *
 * <p>Implementations are expected to maintain an in-memory cache for
 * fast lookup while delegating persistence operations to {@link TagService}.</p>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface TagManager {

  /**
   * Loads all tag-related data from the database into memory.
   *
   * This method initializes or refreshes the in-memory representation
   * of tags and their associations by retrieving the complete tag data
   * set from the underlying database. It should be called during
   * application startup or when a full reload of tag data is needed.
   */
  void loadAllFromDB();

  /**
   * Adds a tag to a shop for a specific player.
   *
   * This method calls the underlying {@code addTag} implementation with default
   * parameters, applying the tag and persisting it to the database.
   *
   * @param shopId the ID of the shop
   * @param player the player applying the tag
   * @param tag    the tag to apply
   *
   * @return the result of the tagging operation
   */
  default TaggingResult addTag(final long shopId, final UUID player, final String tag) {
    return addTag(shopId, player, tag, true);
  }

  /**
   * Adds a tag to a shop for a specific player.
   *
   * @param shopId the ID of the shop
   * @param player the player applying the tag
   * @param tag    the tag to apply
   * @param db     whether to persist the tag to the database
   *
   * @return the result of the tagging operation
   *
   * @since 6.3.0.0
   */
  TaggingResult addTag(long shopId, UUID player, String tag, final boolean db);

  /**
   * Toggles a tag on a shop for a player.
   *
   * <p>If the tag already exists it will be removed,
   * otherwise it will be added.</p>
   *
   * @param shopId the ID of the shop
   * @param player the player toggling the tag
   * @param tag    the tag to toggle
   *
   * @return the result of the operation
   *
   * @since 6.3.0.0
   */
  TaggingResult toggleTag(long shopId, UUID player, String tag);

  /**
   * Removes all tags from all shops for all players.
   *
   * @return true if the operation completed successfully
   *
   * @since 6.3.0.0
   */
  boolean removeAllTags();

  /**
   * Removes all tags associated with a specific shop.
   *
   * @param shopId the shop ID
   *
   * @return true if tags were removed
   *
   * @since 6.3.0.0
   */
  boolean removeAllShopTags(long shopId);

  /**
   * Removes all tags applied by a specific player to a shop.
   *
   * @param shopId the shop ID
   * @param player the player whose tags should be removed
   *
   * @return true if tags were removed
   *
   * @since 6.3.0.0
   */
  boolean removeAllShopTagsBy(long shopId, UUID player);

  /**
   * Removes all tags applied by a player across every shop.
   *
   * @param player the player whose tags should be removed
   *
   * @return true if the operation completed
   *
   * @since 6.3.0.0
   */
  boolean removeAllPlayerTags(UUID player);

  /**
   * Gets the total number of tags currently stored.
   *
   * @return total tag count
   *
   * @since 6.3.0.0
   */
  int totalTags();

  /**
   * Gets the total number of tags applied by a specific player.
   *
   * @param player the player
   *
   * @return number of tags applied by the player
   *
   * @since 6.3.0.0
   */
  int totalTagsByPlayer(UUID player);

  /**
   * Returns a count of tags grouped by shop for a player.
   *
   * <p>The returned {@link TreeMap} maps shop IDs to the number of
   * tags the player has applied to each shop.</p>
   *
   * @param player the player
   *
   * @return a map containing tag counts per shop
   *
   * @since 6.3.0.0
   */
  TreeMap<Long, Integer> tagsCount(UUID player);

  /**
   * Retrieves the tags applied by a player to a specific shop.
   *
   * @param player the player
   * @param shopId the shop ID
   *
   * @return a set of tags applied by the player
   *
   * @since 6.3.0.0
   */
  Set<String> tagsFilteredByShop(UUID player, long shopId);

  /**
   * Retrieves all shop IDs that match a given tag for a player.
   *
   * @param player the player
   * @param tag    the tag to filter by
   *
   * @return list of shop IDs containing the tag
   *
   * @since 6.3.0.0
   */
  List<Long> shopsFilteredByTag(UUID player, String tag);

  /**
   * Retrieves shop IDs matching all provided tags for a player.
   *
   * @param player     the player
   * @param filterTags the tags used as filter criteria
   *
   * @return list of shop IDs matching the filter
   *
   * @since 6.3.0.0
   */
  List<Long> shopsFilteredByTags(UUID player, List<String> filterTags);

  /**
   * Sends a formatted list of shops matching a tag filter to a player.
   *
   * <p>This is typically used by commands such as favorites or watchlists.</p>
   *
   * @param player     the player to send the results to
   * @param filterTags the tags to filter shops by
   * @param type       the type of tag filter (e.g., "favorite", "watchlist")
   * @param titleNode  the message node used for the list title
   * @param noEntries  the message node used when no results exist
   *
   * @since 6.3.0.0
   */
  void listShopsByFilter(Player player, final String commandLabel, final int page, final List<String> filterTags, final String type, String titleNode, String noEntries);

  /**
   * Checks whether a player has applied a specific tag to a shop.
   *
   * @param shopId the shop ID
   * @param player the player
   * @param tag    the tag to check
   *
   * @return true if the tag exists
   *
   * @since 6.3.0.0
   */
  boolean hasTag(long shopId, UUID player, String tag);

  /**
   * Removes a specific tag from all shops for a player.
   *
   * @param player the player
   * @param tag    the tag to remove
   *
   * @return true if the operation completed
   *
   * @since 6.3.0.0
   */
  boolean removeTag(UUID player, String tag);

  /**
   * Removes a specific tag from a single shop for a player.
   *
   * @param shopId the shop ID
   * @param player the player
   * @param tag    the tag to remove
   *
   * @return the result of the removal operation
   *
   * @since 6.3.0.0
   */
  TaggingResult removeTag(long shopId, UUID player, String tag);

  /**
   * Returns the underlying {@link TagService} responsible for persistence and normalization
   * operations.
   *
   * @return the tag service
   *
   * @since 6.3.0.0
   */
  TagService service();
}