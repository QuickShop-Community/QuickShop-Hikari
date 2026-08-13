package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.ShopChunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SimpleShopChunk implements ShopChunk {

  @NotNull
  private final String world;

  private final int x;

  private final int z;

  public SimpleShopChunk(@NotNull final String world, final int x, final int z) {

    this.world = world;
    this.x = x;
    this.z = z;
  }

  @NotNull
  public static ShopChunk fromLocation(@NotNull final Location location) {

    return new SimpleShopChunk(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
  }

  @Override
  @NotNull
  public String getWorld() {

    return world;
  }

  @Override
  public int getX() {

    return x;
  }

  @Override
  public int getZ() {

    return z;
  }

  @Override
  public boolean isSame(@NotNull final String world, final int x, final int z) {

    return this.x == x && this.z == z && this.world.equals(world);
  }

  @Override
  public boolean isSame(@NotNull final World world, final int x, final int z) {

    return isSame(world.getName(), x, z);
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof SimpleShopChunk)) return false;
    final SimpleShopChunk other = (SimpleShopChunk)o;
    return this.getX() == other.getX()
           && this.getZ() == other.getZ()
           && Objects.equals(this.getWorld(), other.getWorld());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getX(), this.getZ(), this.getWorld());
  }

  @Override
  public String toString() {

    return "SimpleShopChunk(world=" + this.getWorld() + ", x=" + this.getX() + ", z=" + this.getZ() + ")";
  }
}
