package com.ghostchu.quickshop.api.shop;


/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import org.bukkit.Location;

/**
 * Locatable
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface Locatable<T> {

  /**
   * Retrieves the location associated with this object.
   *
   * @return the location of type T associated with this object.
   */
  T getLocation();

  /**
   * Converts the location associated with this object to a Bukkit-compatible {@link Location}.
   *
   * @return the Bukkit {@link Location} representation of this object's location.
   */
  Location bukkitLocation();
}