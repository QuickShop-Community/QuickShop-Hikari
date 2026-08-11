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

import java.util.Set;

/**
 * Represents a per-player tag index used for fast lookup of tags and shops.
 *
 * <p>This index maintains a bidirectional mapping between shops and tags:</p>
 * <ul>
 *   <li>shopId → tags</li>
 *   <li>tag → shopIds</li>
 * </ul>
 *
 * <p>Implementations are expected to be thread-safe.</p>
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface PlayerTagIndex {

  /**
   * Adds a tag entry to the index.
   *
   * @param shopId the shop ID
   * @param tag    the tag
   *
   * @since 6.3.0.0
   */
  void addTag(long shopId, String tag);

  /**
   * Removes a tag entry from the index.
   *
   * @param shopId the shop ID
   * @param tag    the tag
   *
   * @since 6.3.0.0
   */
  void removeTag(long shopId, String tag);

  /**
   * Returns the tags applied to a shop.
   *
   * @param shopId the shop ID
   *
   * @return tags associated with the shop
   *
   * @since 6.3.0.0
   */
  Set<String> getTags(long shopId);

  /**
   * Returns all shops associated with a tag.
   *
   * @param tag the tag
   *
   * @return shop IDs containing the tag
   *
   * @since 6.3.0.0
   */
  Set<Long> getShops(String tag);

  /**
   * Returns all shop IDs currently tracked by this index.
   *
   * @return tracked shop IDs
   *
   * @since 6.3.0.0
   */
  Set<Long> shops();

  /**
   * Returns the total number of tags tracked by this index.
   *
   * @return total tag count
   *
   * @since 6.3.0.0
   */
  int totalTags();

  /**
   * Checks whether a shop has a specific tag.
   *
   * @param shopId the shop ID
   * @param tag    the tag
   *
   * @return true if the tag exists
   *
   * @since 6.3.0.0
   */
  boolean hasTag(long shopId, String tag);

  /**
   * Clears all cached tag data.
   *
   * @since 6.3.0.0
   */
  void clear();
}