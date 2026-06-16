package com.ghostchu.quickshop.api.shop.interaction;
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

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * InteractionHandler
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface InteractionType {

  /**
   * Retrieves the identifier associated with this type of interaction.
   *
   * @return The identifier for this type of interaction.
   */
  String identifier();

  /**
   * Checks if this type of interaction applies to the given PlayerInteractEvent with the specified
   * InteractionClick action.
   *
   * @param event The PlayerInteractEvent to check against.
   * @param click The type of interaction click to consider.
   *
   * @return True if this interaction type applies to the event with the given InteractionClick,
   * false otherwise.
   */
  boolean applies(final @NotNull PlayerInteractEvent event, final @NotNull InteractionClick click);

  /**
   * Determines if this type of interaction applies to the given PlayerInteractEntityEvent
   * with the specified InteractionClick type.
   *
   * @param event The PlayerInteractEntityEvent to evaluate. Must not be null.
   * @param click The type of interaction click to consider. Must not be null.
   * @return True if this interaction type applies to the event with the given InteractionClick,
   * false otherwise.
   */
  default boolean applies(final @NotNull PlayerInteractEntityEvent event, final @NotNull InteractionClick click) {
    return false;
  }

  /**
   * Determines if this type of interaction applies to the given EntityDamageByEntityEvent with the specified
   * InteractionClick action.
   * @param event The EntityDamageByEntityEvent to check against. Must not be null.
   * @param click The type of interaction click to consider. Must not be null.
   * @return True if this interaction type applies to the event with the given InteractionClick, false otherwise.
   */
  default boolean applies(final @NotNull EntityDamageByEntityEvent event, final @NotNull InteractionClick click) {
    return false;
  }
}