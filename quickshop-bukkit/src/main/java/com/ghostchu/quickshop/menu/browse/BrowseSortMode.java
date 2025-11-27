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
 * BrowseSortMode - Sorting options for the browse GUI
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public enum BrowseSortMode {
  
  /**
   * Sort by price, lowest first
   */
  PRICE_ASC("price-low-high", "gui.browse.sort.price-asc"),
  
  /**
   * Sort by price, highest first
   */
  PRICE_DESC("price-high-low", "gui.browse.sort.price-desc"),
  
  /**
   * Sort by stock amount, highest first
   */
  STOCK("stock", "gui.browse.sort.stock"),
  
  /**
   * Sort by item name alphabetically
   */
  NAME("name", "gui.browse.sort.name");
  
  private final String id;
  private final String translationKey;
  
  BrowseSortMode(final String id, final String translationKey) {
    this.id = id;
    this.translationKey = translationKey;
  }
  
  public String getId() {
    return id;
  }
  
  public String getTranslationKey() {
    return translationKey;
  }
  
  /**
   * Get the next sort mode in the cycle
   * @return The next sort mode
   */
  public BrowseSortMode next() {
    final BrowseSortMode[] values = values();
    return values[(this.ordinal() + 1) % values.length];
  }
  
  /**
   * Get sort mode from id string
   * @param id The id string
   * @return The sort mode, or PRICE_ASC if not found
   */
  public static BrowseSortMode fromId(final String id) {
    for (final BrowseSortMode mode : values()) {
      if (mode.getId().equalsIgnoreCase(id)) {
        return mode;
      }
    }
    return PRICE_ASC;
  }
}
