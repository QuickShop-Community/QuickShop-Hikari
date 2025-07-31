package com.ghostchu.quickshop.api.economyrevamp;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * EconomyManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface EconomyManager {

  /**
   * Retrieves a map of all economies associated with their unique identifier.
   *
   * @return A map containing integers as keys and EconomyProvider objects as values.
   */
  Map<String, EconomyProvider> providers();

  /**
   * Set the EconomyProvider for this manager.
   *
   * @param provider The EconomyProvider to be set for this manager.
   */
  void provider(final @NotNull EconomyProvider provider);

  /**
   * Retrieves an Optional instance of EconomyProvider associated with the given id.
   *
   * @param id The unique identifier of the EconomyProvider to retrieve.
   *
   * @return An Optional instance containing the EconomyProvider if found, or an empty Optional if
   * not found.
   */
  Optional<EconomyProvider> provider(final @NotNull String id);

  /**
   * Uses the provided EconomyProvider associated with the specified provider ID.
   *
   * @param id The unique identifier of the EconomyProvider to be used.
   */
  void useProvider(final @NotNull String id);

  /**
   * Retrieves the current EconomyProvider.
   *
   * @return The current EconomyProvider, or null if not set.
   */
  @Nullable
  EconomyProvider provider();
}