package com.github.quickshopcommunity.dominion.util;
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
import org.bukkit.World;

/**
 * ChunkBounds
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class ChunkBounds {

  private final World world;
  private final int chunkX;
  private final int chunkZ;

  private final int minX;
  private final int maxX;
  private final int minZ;
  private final int maxZ;

  public ChunkBounds(final World world, final int chunkX, final int chunkZ) {

    this.world = world;

    this.chunkX = chunkX;
    this.chunkZ = chunkZ;

    this.minX = chunkX << 4;
    this.maxX = minX + 15;

    this.minZ = chunkZ << 4;
    this.maxZ = minZ + 15;
  }

  public Location min() {

    return new Location(world, minX, world.getMinHeight(), minZ);
  }

  public Location max() {

    return new Location(world, minX, world.getMaxHeight(), minZ);
  }
}