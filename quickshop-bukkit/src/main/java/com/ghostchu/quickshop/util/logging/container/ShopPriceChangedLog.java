package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;

import java.util.Objects;

public class ShopPriceChangedLog {

  private static int v = 2;
  private ShopInfoStorage shop;
  private double oldPrice;
  private double newPrice;

  public ShopPriceChangedLog(final ShopInfoStorage shop, final double oldPrice, final double newPrice) {

    this.shop = shop;
    this.oldPrice = oldPrice;
    this.newPrice = newPrice;
  }

  public ShopInfoStorage getShop() {

    return this.shop;
  }

  public double getOldPrice() {

    return this.oldPrice;
  }

  public double getNewPrice() {

    return this.newPrice;
  }

  public void setShop(final ShopInfoStorage shop) {

    this.shop = shop;
  }

  public void setOldPrice(final double oldPrice) {

    this.oldPrice = oldPrice;
  }

  public void setNewPrice(final double newPrice) {

    this.newPrice = newPrice;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopPriceChangedLog)) return false;
    final ShopPriceChangedLog other = (ShopPriceChangedLog)o;
    return Double.compare(this.getOldPrice(), other.getOldPrice()) == 0
           && Double.compare(this.getNewPrice(), other.getNewPrice()) == 0
           && Objects.equals(this.getShop(), other.getShop());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getOldPrice(), this.getNewPrice(), this.getShop());
  }

  @Override
  public String toString() {

    return "ShopPriceChangedLog(shop=" + this.getShop() + ", oldPrice=" + this.getOldPrice() + ", newPrice=" + this.getNewPrice() + ")";
  }
}
