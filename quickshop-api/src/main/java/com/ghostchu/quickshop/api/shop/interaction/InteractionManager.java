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

import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * InteractionManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface InteractionManager {

  /**
   * Registers an InteractionBehavior.
   *
   * @param behavior the behavior to register.
   */
  void behavior(@NotNull InteractionBehavior behavior);

  /**
   * Retrieves a behavior by identifier.
   *
   * @param identifier the behavior identifier (case-insensitive).
   *
   * @return an optional containing the behavior, or empty if not found.
   */
  Optional<InteractionBehavior> behavior(@NotNull String identifier);

  /**
   * Retrieves the behavior mapped to the given interaction.
   *
   * @param interaction the interaction to resolve.
   *
   * @return an optional containing the mapped behavior.
   */
  Optional<InteractionBehavior> behavior(@NotNull InteractionType interaction);

  /**
   * Checks if a behavior is registered.
   *
   * @param identifier the identifier to check.
   *
   * @return true if the behavior exists.
   */
  boolean hasBehavior(@NotNull String identifier);

  /**
   * Gets all registered behaviors.
   *
   * @return an unmodifiable collection of behaviors.
   */
  Collection<InteractionBehavior> getBehaviors();

  /**
   * Registers an interaction type.
   *
   * @param interaction the interaction to register.
   */
  void interaction(@NotNull InteractionType interaction);

  /**
   * Retrieves an interaction by identifier.
   *
   * @param identifier the interaction identifier.
   *
   * @return an optional containing the interaction if found.
   */
  Optional<InteractionType> interaction(@NotNull String identifier);

  /**
   * Resolves an interaction from an event and click context.
   *
   * @param event the PlayerInteractEvent.
   * @param click the click context.
   *
   * @return an optional containing a matching interaction.
   */
  Optional<InteractionType> interaction(@NotNull PlayerInteractEvent event, @NotNull InteractionClick click);

  /**
   * Checks if an interaction is registered.
   *
   * @param identifier the interaction identifier.
   *
   * @return true if the interaction exists.
   */
  boolean hasInteraction(@NotNull String identifier);

  /**
   * Gets all registered interactions.
   *
   * @return an unmodifiable collection of interactions.
   */
  Collection<InteractionType> getInteractions();
}