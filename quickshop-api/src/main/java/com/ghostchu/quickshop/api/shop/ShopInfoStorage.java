package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.serialize.BlockPos;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Minimal information about a shop.
 */
public class ShopInfoStorage {

  private final String world;
  private final BlockPos position;
  private final String owner;
  private final double price;
  private final String item;
  private final int unlimited;
  private final int shopType;
  private final String extra;
  private final String currency;
  private final boolean disableDisplay;
  private final String taxAccount;
  private final String inventoryWrapperName;
  private final String symbolLink;
  private final Map<UUID, String> permission;

  public ShopInfoStorage(final String world, final BlockPos position, final QUser owner, final double price, final String item, final int unlimited, final int shopType, final String extra, final String currency, final boolean disableDisplay, final QUser taxAccount, final String inventoryWrapperName, final String symbolLink, final Map<UUID, String> permission) {

    this.world = world;
    this.position = position;
    this.owner = owner.serialize();
    this.price = price;
    this.item = item;
    this.unlimited = unlimited;
    this.shopType = shopType;
    this.extra = extra;
    this.currency = currency;
    this.disableDisplay = disableDisplay;
    if(taxAccount != null) {
      this.taxAccount = taxAccount.serialize();
    } else {
      this.taxAccount = null;
    }
    this.inventoryWrapperName = inventoryWrapperName;
    this.symbolLink = symbolLink;
    this.permission = permission;
  }

  public String getWorld() {

    return this.world;
  }

  public BlockPos getPosition() {

    return this.position;
  }

  public String getOwner() {

    return this.owner;
  }

  public double getPrice() {

    return this.price;
  }

  public String getItem() {

    return this.item;
  }

  public int getUnlimited() {

    return this.unlimited;
  }

  public int getShopType() {

    return this.shopType;
  }

  public String getExtra() {

    return this.extra;
  }

  public String getCurrency() {

    return this.currency;
  }

  public boolean isDisableDisplay() {

    return this.disableDisplay;
  }

  public String getTaxAccount() {

    return this.taxAccount;
  }

  public String getInventoryWrapperName() {

    return this.inventoryWrapperName;
  }

  public String getSymbolLink() {

    return this.symbolLink;
  }

  public Map<UUID, String> getPermission() {

    return this.permission;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopInfoStorage)) return false;
    final ShopInfoStorage other = (ShopInfoStorage)o;
    return Double.compare(this.getPrice(), other.getPrice()) == 0
           && this.getUnlimited() == other.getUnlimited()
           && this.getShopType() == other.getShopType()
           && this.isDisableDisplay() == other.isDisableDisplay()
           && Objects.equals(this.getWorld(), other.getWorld())
           && Objects.equals(this.getPosition(), other.getPosition())
           && Objects.equals(this.getOwner(), other.getOwner())
           && Objects.equals(this.getItem(), other.getItem())
           && Objects.equals(this.getExtra(), other.getExtra())
           && Objects.equals(this.getCurrency(), other.getCurrency())
           && Objects.equals(this.getTaxAccount(), other.getTaxAccount())
           && Objects.equals(this.getInventoryWrapperName(), other.getInventoryWrapperName())
           && Objects.equals(this.getSymbolLink(), other.getSymbolLink())
           && Objects.equals(this.getPermission(), other.getPermission());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getPrice(), this.getUnlimited(), this.getShopType(), this.isDisableDisplay(), this.getWorld(), this.getPosition(), this.getOwner(), this.getItem(), this.getExtra(), this.getCurrency(), this.getTaxAccount(), this.getInventoryWrapperName(), this.getSymbolLink(), this.getPermission());
  }

  @Override
  public String toString() {

    return "ShopInfoStorage(world=" + this.getWorld() + ", position=" + this.getPosition() + ", owner=" + this.getOwner() + ", price=" + this.getPrice() + ", item=" + this.getItem() + ", unlimited=" + this.getUnlimited() + ", shopType=" + this.getShopType() + ", extra=" + this.getExtra() + ", currency=" + this.getCurrency() + ", disableDisplay=" + this.isDisableDisplay() + ", taxAccount=" + this.getTaxAccount() + ", inventoryWrapperName=" + this.getInventoryWrapperName() + ", symbolLink=" + this.getSymbolLink() + ", permission=" + this.getPermission() + ")";
  }
}
