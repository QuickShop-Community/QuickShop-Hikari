package com.ghostchu.quickshop.api.shop.display;

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

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * DisplayItem
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface DisplayItem {

  /**
   * Gets the display location for an item. If it is a double shop and it is not the left shop, it
   * will average the locations of the two chests comprising it to be perfectly in the middle. If it
   * is the left shop, it will return null since the left shop does not spawn an item. Otherwise, it
   * will give you the middle of the single chest.
   *
   * @return The Location that the item *should* be displaying at.
   */
  @Nullable Location getDisplayLocation();

  /**
   * Checks whether the display item has been moved from its intended location.
   *
   * @return true if the display item has been moved, false otherwise
   */
  boolean checkDisplayIsMoved();

  /**
   * Checks whether the display item needs to regenerate its state or visual representation.
   *
   * @return true if the display item requires regeneration, false otherwise
   */
  boolean checkDisplayNeedRegen();

  /**
   * Checks whether the given entity is associated with a shop.
   *
   * @param entity the entity to check
   * @return true if the entity is part of a shop, false otherwise
   */
  boolean checkIsShopEntity(Entity entity);

  /**
   * Retrieves the display entity associated with this shop display.
   *
   * @return The entity that represents the visual display of the shop, or null if no display entity is present.
   */
  Entity getDisplay();

  /**
   * Restores the display entity to its intended location if it has been moved.
   *
   * This method is responsible for repositioning the shop's display entity
   * back to its designated location if it has been detected as being moved
   * from its intended position. The method ensures that the visual representation
   * of the shop remains consistent and properly aligned with the shop's mechanics.
   *
   * It may internally utilize location data, such as the result of {@code getDisplayLocation()},
   * for determining the correct position and validating the current position.
   */
  void fixDisplayMoved();

  /**
   * Ensures the display entity's state or visual representation is regenerated when needed.
   *
   * This method is responsible for handling cases where the display entity associated with a shop
   * needs to be recreated or visually refreshed. Regeneration may involve updating the display's
   * appearance, state, or position to ensure it aligns with the current shop settings and mechanics.
   *
   * It is typically invoked when the display is flagged for regeneration, such as after a configuration
   * change or when a visual inconsistency is detected.
   */
  void fixDisplayNeedRegen();

  /**
   * Checks whether the display item is currently spawned in the world.
   *
   * @return true if the display item is currently spawned, false otherwise
   */
  boolean isSpawned();

  /**
   * Determines whether the display item is relevant or applicable to the specified player.
   *
   * @param player the player to check applicability for, cannot be null
   * @return true if the display item is applicable to the given player, false otherwise
   */
  boolean isApplicableForPlayer(Player player);

  /**
   * Checks whether the display item is flagged for removal from the world.
   *
   * This method determines if the associated display item is in a state
   * where it is marked for removal but has not yet been removed.
   *
   * @return true if the display item is pending removal, false otherwise
   */
  boolean isPendingRemoval();

  /**
   * Marks the display item for removal from the world.
   *
   * This method is responsible for initiating the removal process of the associated display item.
   * When invoked, the display item transitions into a state where it is marked for removal.
   * The actual removal process may be deferred until certain conditions are met or when the
   * system is ready to complete the removal.
   *
   * Implementations may use this method to safely handle removal operations while maintaining
   * consistency in the system's state.
   */
  void pendingRemoval();

  /**
   * Removes the display item from the system, with an option to preserve its state in the world.
   *
   * @param dontTouchWorld if true, the display item's state in the world will not be altered; if false,
   *                       the display item will also be removed from the world.
   */
  void remove(boolean dontTouchWorld);

  /**
   * Respawns the display item associated with the shop in its correct state and location.
   *
   * This method is responsible for recreating and reinitializing the display entity
   * in situations where it may have been removed, despawned, or otherwise
   * misplaced. It ensures that the display item is properly represented within
   * the world and aligned with the shop's mechanics.
   *
   * Implementations may internally manage visual configuration, spawn location,
   * and state synchronization. This ensures that the display item maintains
   * consistency with the intended state of the shop at all times.
   */
  void respawn();

  /**
   * Ensures the given entity is safeguarded, typically for maintaining correct state,
   * alignment, or protection within the system. This method may involve verifying
   * the entity's position, state, or association, ensuring it adheres to the intended mechanics.
   *
   * @param entity the entity to be safeguarded, must not be null
   */
  void safeGuard(@NotNull Entity entity);

  /**
   * Spawns the display item associated with the shop into the world.
   *
   * This method is responsible for initializing and placing the display item
   * at its correct location in the world. It ensures that the item is visible
   * and interacts properly with the shop's mechanics. The display item may be
   * represented as a physical entity, virtual item, or any other visual representation
   * supported by the shop system.
   *
   * Implementations may handle visual positioning, state initialization, and
   * alignment with the intended shop configuration. The method does not handle
   * despawning or removal of the display item; those actions are managed by separate
   * methods.
   */
  void spawn();

  /**
   * Retrieves the shop associated with this display item.
   * The shop provides access to its various components, such as item, interaction, lifecycle,
   * metadata, permissions, pricing, and trading mechanisms.
   *
   * @return The {@link ModernShop} associated with this display item, parameterized with types
   *         for price, location, and player.
   */
  ModernShop<?, ?, ?> shop();
}