package com.ghostchu.quickshop.api.shop.display;
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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A generic class representing a PacketFactory that can create packets of type T.
 * This class can be extended to provide specific implementations for creating packets based on
 * different versions or criteria.
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public interface PacketFactory<T> {

  /**
   * Creates a spawn packet for the specified ID and display location.
   *
   * @param id the ID of the packet to be created
   * @param displayLocation the display location where the packet will be spawned
   * @return the spawn packet of type T
   */
  T createSpawnPacket(final int id, @NotNull Location displayLocation);

  /**
   * Creates a metadata packet with the specified ID and ItemStack.
   *
   * @param id the ID of the metadata packet to be created
   * @param itemStack the ItemStack to include in the metadata packet
   * @return the metadata packet of type T
   */
  T createMetaDataPacket(final int id, @NotNull ItemStack itemStack);

  /**
   * Creates a velocity packet with the specified ID.
   *
   * @param id the ID of the velocity packet to be created
   * @return the velocity packet of type T
   */
  T createVelocityPacket(final int id);

  /**
 * Creates a destroy packet for the given ID.
 *
 * @param id the ID of the packet to be destroyed
 * @return the destroy packet of type T
 */
  T createDestroyPacket(final int id);

  /**
   * Sends the specified packet to the given player.
   *
   * @param player the player to receive the packet, cannot be null
   * @param packet the packet of type T to be sent, cannot be null
   * @return true if the packet was successfully sent, false otherwise
   */
  boolean sendPacket(@NotNull Player player, @NotNull T packet);

  /**
   * Registers the method to listen to the packet sending chunk data.
   */
  void registerSendChunk();

  /**
   * Unregisters the method to listen to the packet sending chunk data.
   */
  void unregisterSendChunk();

  /**
   * Registers the method to listen to the packet sending the unloading of a chunk.
   */
  void registerUnloadChunk();

  /**
   * Unregisters the method to listen to the packet sending the unloading of a chunk.
   */
  void unregisterUnloadChunk();
}