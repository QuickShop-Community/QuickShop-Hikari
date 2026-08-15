package com.ghostchu.quickshop.api.event.inventory;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.Shop;

/**
 * Fire when shop inventory space/stock calculating.
 */
public class ShopInventoryCalculateEvent extends AbstractQSEvent {

  private final Shop shop;
  private final int space;
  private final int stock;

  public ShopInventoryCalculateEvent(final Shop shop, final int space, final int stock) {

    this.shop = shop;
    this.space = space;
    this.stock = stock;
  }

  /**
   * Gets the shop that inventory has been calculated
   *
   * @return The shop
   */
  public Shop getShop() {

    return shop;
  }

  /**
   * Getting the inventory space
   *
   * @return The inventory space (-1 if not get calculated)
   */
  public int getSpace() {

    return space;
  }

  /**
   * Getting the inventory stock
   *
   * @return The inventory stock (-1 if not get calculated)
   */
  public int getStock() {

    return stock;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopInventoryCalculateEvent)) return false;
    final ShopInventoryCalculateEvent other = (ShopInventoryCalculateEvent)o;
    if(!other.canEqual((Object)this)) return false;
    if(!super.equals(o)) return false;
    if(this.getSpace() != other.getSpace()) return false;
    if(this.getStock() != other.getStock()) return false;
    final Object thisShop = this.getShop();
    final Object otherShop = other.getShop();
    if(thisShop == null? otherShop != null : !thisShop.equals(otherShop)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {

    return other instanceof ShopInventoryCalculateEvent;
  }

  @Override
  public int hashCode() {

    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getSpace();
    result = result * PRIME + this.getStock();
    final Object shopValue = this.getShop();
    result = result * PRIME + (shopValue == null? 43 : shopValue.hashCode());
    return result;
  }

  @Override
  public String toString() {

    return "ShopInventoryCalculateEvent(shop=" + this.getShop() + ", space=" + this.getSpace() + ", stock=" + this.getStock() + ")";
  }
}
