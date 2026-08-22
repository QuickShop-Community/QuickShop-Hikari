package com.ghostchu.quickshop.shop;

import java.util.Objects;

/**
 * TODO This class used for storage the shop sign
 */
public class ShopSignStorage {

  private static final boolean SHOP_SIGN = true;
  private String world;
  private int x;
  private int y;
  private int z;

  public ShopSignStorage(final String world, final int x, final int y, final int z) {

    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public boolean equals(final String world, final int x, final int y, final int z) {

    return Objects.equals(this.world, world) && this.x == x && this.y == y && this.z == z;
  }

  public static class ShopSignStorageBuilder {

    private String world;
    private int x;
    private int y;
    private int z;

    ShopSignStorageBuilder() {

  }

    public ShopSignStorage.ShopSignStorageBuilder world(final String world) {

      this.world = world;
      return this;
    }

    public ShopSignStorage.ShopSignStorageBuilder x(final int x) {

      this.x = x;
      return this;
    }

    public ShopSignStorage.ShopSignStorageBuilder y(final int y) {

      this.y = y;
      return this;
    }

    public ShopSignStorage.ShopSignStorageBuilder z(final int z) {

      this.z = z;
      return this;
    }

    public ShopSignStorage build() {

      return new ShopSignStorage(this.world, this.x, this.y, this.z);
    }

    @Override
    public String toString() {

      return "ShopSignStorage.ShopSignStorageBuilder(world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
    }
  }

  public static ShopSignStorage.ShopSignStorageBuilder builder() {

    return new ShopSignStorage.ShopSignStorageBuilder();
  }

  public String getWorld() {

    return this.world;
  }

  public int getX() {

    return this.x;
  }

  public int getY() {

    return this.y;
  }

  public int getZ() {

    return this.z;
  }

  public void setWorld(final String world) {

    this.world = world;
  }

  public void setX(final int x) {

    this.x = x;
  }

  public void setY(final int y) {

    this.y = y;
  }

  public void setZ(final int z) {

    this.z = z;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopSignStorage)) return false;
    final ShopSignStorage other = (ShopSignStorage)o;
    return this.getX() == other.getX()
           && this.getY() == other.getY()
           && this.getZ() == other.getZ()
           && Objects.equals(this.getWorld(), other.getWorld());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getX(), this.getY(), this.getZ(), this.getWorld());
  }

  @Override
  public String toString() {

    return "ShopSignStorage(world=" + this.getWorld() + ", x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
  }
}
