package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;

import java.util.Objects;

public class ShopCreationLog {

  private static int v = 2;
  private String creator;
  private ShopInfoStorage shop;
  private BlockPos location;

  public ShopCreationLog(final QUser creator, final ShopInfoStorage shop, final BlockPos location) {

    this.creator = creator.serialize();
    this.shop = shop;
    this.location = location;
  }

  public String getCreator() {

    return this.creator;
  }

  public ShopInfoStorage getShop() {

    return this.shop;
  }

  public BlockPos getLocation() {

    return this.location;
  }

  public void setCreator(final String creator) {

    this.creator = creator;
  }

  public void setShop(final ShopInfoStorage shop) {

    this.shop = shop;
  }

  public void setLocation(final BlockPos location) {

    this.location = location;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopCreationLog)) return false;
    final ShopCreationLog other = (ShopCreationLog)o;
    return Objects.equals(this.getCreator(), other.getCreator())
           && Objects.equals(this.getShop(), other.getShop())
           && Objects.equals(this.getLocation(), other.getLocation());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getCreator(), this.getShop(), this.getLocation());
  }

  @Override
  public String toString() {

    return "ShopCreationLog(creator=" + this.getCreator() + ", shop=" + this.getShop() + ", location=" + this.getLocation() + ")";
  }
}
