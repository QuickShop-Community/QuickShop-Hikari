package com.ghostchu.quickshop.api.shop.display;
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

import java.util.Map;
import java.util.Optional;

/**
 * An interface representing a PacketHandler that processes packets.
 *
 * @param <T> Is the instance to the packet library's internal API for creating/sending packets.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public interface PacketHandler<T> {

  /**
   * Retrieves a map of PacketFactory instances keyed by the game version supported.
   *
   * @return Map of PacketFactory instances where keys are the game version supported.
   */
  Map<String, PacketFactory<?>> factories();

  /**
   * Retrieves the identifier for the PacketHandler.
   *
   * @return The identifier for the PacketHandler.
   */
  String identifier();

  /**
   * Retrieves the name of the plugin.
   *
   * @return The name of the plugin as a String.
   */
  String pluginName();

  /**
   * Initializes the object or resource.
   */
  void initialize();

  /**
   * Retrieves an Optional instance of PacketFactory based on the provided version.
   *
   * @param version The version of the PacketFactory to retrieve.
   * @return An Optional instance of PacketFactory if available for the given version.
   */
  default Optional<PacketFactory<?>> factory(final String version) {

    return Optional.ofNullable(factories().get(version));
  }

  T internal();
}