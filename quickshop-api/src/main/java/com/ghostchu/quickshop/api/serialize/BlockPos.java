package com.ghostchu.quickshop.api.serialize;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

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

    return "BlockPos{" + "version=" + version + ", x=" + x + ", y=" + y + ", z=" + z + ", world='" + world + '\'' + '}';
  }

  public String serialize() {

    return version + ";" + x + ";" + y + ";" + z + ";" + world;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof BlockPos)) return false;
    final BlockPos other = (BlockPos)o;
    if(this.version != other.version) return false;
    if(this.getX() != other.getX()) return false;
    if(this.getY() != other.getY()) return false;
    if(this.getZ() != other.getZ()) return false;
    final Object thisWorld = this.getWorld();
    final Object otherWorld = other.getWorld();
    if(thisWorld == null? otherWorld != null : !thisWorld.equals(otherWorld)) return false;
    return true;
  }

  @Override
  public int hashCode() {

    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.version;
    result = result * PRIME + this.getX();
    result = result * PRIME + this.getY();
    result = result * PRIME + this.getZ();
    final Object worldValue = this.getWorld();
    result = result * PRIME + (worldValue == null? 43 : worldValue.hashCode());
    return result;
  }
}
