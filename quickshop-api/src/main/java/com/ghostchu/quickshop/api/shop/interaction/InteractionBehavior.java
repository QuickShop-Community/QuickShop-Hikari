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

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * InteractionBehavior
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface InteractionBehavior {

  /**
   * Retrieves the identifier associated with this InteractionBehavior.
   *
   * @return the identifier as a String.
   */
  String identifier();

  /**
   * Used to handle interactions that a player has with a shop.
   *
   * @param shop        The shop involved in the interaction, not null.
   * @param player      The player involved in the interaction, not null.
   * @param event       The PlayerInteractEvent that triggered the interaction, not null.
   * @param clickType   The type of click that triggered the interaction, not null.
   * @param interaction The type of interaction that occurred, can be null.
   */
  void handle(final @NotNull QuickShopAPI plugin,
              final @Nullable Shop shop,
              final @NotNull Player player,
              final @NotNull PlayerInteractEvent event,
              final @NotNull InteractionClick clickType,
              final @Nullable InteractionType interaction);

  /**
   * Handles player interactions with entities in the context of a shop.
   *
   * @param plugin      The QuickShopAPI instance used for handling the interaction, not null.
   * @param shop        The shop involved in the interaction, can be null.
   * @param player      The player involved in the interaction, not null.
   * @param event       The PlayerInteractEntityEvent that triggered the interaction, not null.
   * @param clickType   The type of click that triggered the interaction, not null.
   * @param interaction The type of interaction that occurred, can be null.
   */
  default void handle(final @NotNull QuickShopAPI plugin,
                      final @Nullable Shop shop,
                      final @NotNull Player player,
                      final @NotNull PlayerInteractEntityEvent event,
                      final @NotNull InteractionClick clickType,
                      final @Nullable InteractionType interaction) {

  }

  /**
   * Handles interactions where a player damages an entity in the context of a shop.
   *
   * @param plugin      The QuickShopAPI instance used for handling the interaction, not null.
   * @param shop        The shop involved in the interaction, can be null.
   * @param player      The player involved in the interaction, not null.
   * @param event       The EntityDamageByEntityEvent that triggered the interaction, not null.
   * @param clickType   The type of click that triggered the interaction, not null.
   * @param interaction The type of interaction that occurred, can be null.
   */
  default void handle(final @NotNull QuickShopAPI plugin,
                      final @Nullable Shop shop,
                      final @NotNull Player player,
                      final @NotNull EntityDamageByEntityEvent event,
                      final @NotNull InteractionClick clickType,
                      final @Nullable InteractionType interaction) {}
}