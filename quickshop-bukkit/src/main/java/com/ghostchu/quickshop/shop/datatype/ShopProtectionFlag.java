package com.ghostchu.quickshop.shop.datatype;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShopProtectionFlag {

  private static final String MARK = "QuickShop DisplayItem";
  private final String itemStackString;
  private final String shopLocation;

  public ShopProtectionFlag(@NotNull final String shopLocation, @NotNull final String itemStackString) {

    this.shopLocation = shopLocation;
    this.itemStackString = itemStackString;
  }

  public static String getMark() {

    return getDefaultMark();
  }

  public static String getDefaultMark() {

    return MARK;
  }

  public String getItemStackString() {

    return this.itemStackString;
  }

  public String getShopLocation() {

    return this.shopLocation;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopProtectionFlag)) return false;
    final ShopProtectionFlag other = (ShopProtectionFlag)o;
    return Objects.equals(this.getItemStackString(), other.getItemStackString())
           && Objects.equals(this.getShopLocation(), other.getShopLocation());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getItemStackString(), this.getShopLocation());
  }

  @Override
  public String toString() {

    return "ShopProtectionFlag(itemStackString=" + this.getItemStackString() + ", shopLocation=" + this.getShopLocation() + ")";
  }
}
