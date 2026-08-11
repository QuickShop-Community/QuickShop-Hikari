package com.ghostchu.quickshop.menu.browse;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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

/**
 * BrowseFilterMode - Filter options for the browse GUI
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public enum BrowseFilterMode {

  /**
   * Show all shops
   */
  ALL("all", "gui.browse.filter.all", "gui.browse.filter.all-indicator"),

  /**
   * Show only buying shops (shops that buy from players)
   */
  BUYING("buying", "gui.browse.filter.buying", "gui.browse.filter.buying-indicator"),

  /**
   * Show only selling shops (shops that sell to players)
   */
  SELLING("selling", "gui.browse.filter.selling", "gui.browse.filter.selling-indicator");

  private final String id;
  private final String translationKey;
  private final String indicatorTranslationKey;

  BrowseFilterMode(final String id, final String translationKey, final String indicatorTranslationKey) {

    this.id = id;
    this.translationKey = translationKey;
    this.indicatorTranslationKey = indicatorTranslationKey;
  }

  /**
   * Get filter mode from id string
   *
   * @param id The id string
   *
   * @return The filter mode, or ALL if not found
   */
  public static BrowseFilterMode fromId(final String id) {

    for(final BrowseFilterMode mode : values()) {
      if(mode.getId().equalsIgnoreCase(id)) {
        return mode;
      }
    }
    return ALL;
  }

  public String getId() {

    return id;
  }

  public String getTranslationKey() {

    return translationKey;
  }

  public String indicatorTranslationKey() {

    return indicatorTranslationKey;
  }

  /**
   * Get the next filter mode in the cycle
   *
   * @return The next filter mode
   */
  public BrowseFilterMode next() {

    final BrowseFilterMode[] values = values();
    return values[(this.ordinal() + 1) % values.length];
  }
}
