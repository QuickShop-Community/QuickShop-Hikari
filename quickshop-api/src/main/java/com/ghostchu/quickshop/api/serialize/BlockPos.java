package com.ghostchu.quickshop.api.serialize;

import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode
public final class BlockPos {

  private final int version = 2;
  private final int x;
  private final int y;
  private final int z;
  private final String world;

  public BlockPos(@NotNull final Location location) {

    this.x = location.getBlockX();
    this.y = location.getBlockY();
    this.z = location.getBlockZ();
    this.world = location.getWorld().getName();
  }

  public BlockPos(final int x, final int y, final int z, final String world) {

    this.x = x;
    this.y = y;
    this.z = z;
    this.world = world;
  }

  public int getX() {

    return x;
  }


  public int getY() {

    return y;
  }


  public int getZ() {

    return z;
  }

  public String getWorld() {

    return world;
  }

  @Override
  public String toString() {

    return "BlockPos{" +
           "version=" + version +
           ", x=" + x +
           ", y=" + y +
           ", z=" + z +
           ", world='" + world + '\'' +
           '}';
  }

  public static BlockPos deserialize(final String string) {

    try {
      final String[] split = string.split(";");
      if(split.length < 5) {
        throw new IllegalArgumentException("Invalid input string for deserialization");
      }
      return new BlockPos(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4]);
    } catch(NumberFormatException e) {
      throw new IllegalArgumentException("Failed to parse integer during deserialization", e);
    }
  }

  public String serialize() {

    return version + ";" + x + ";" + y + ";" + z + ";" + world;
  }
}
