package com.ghostchu.quickshop.shop.cache;

import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;

public class SimpleShopInventoryCountCache implements ShopInventoryCountCache {

  private int stock;
  private int space;

  public SimpleShopInventoryCountCache(int stock, int space) {

    this.stock = stock;
    this.space = space;
  }

  @Override
  public int getStock() {

    return stock;
  }

  public void setStock(int stock) {

    this.stock = stock;
  }

  @Override
  public int getSpace() {

    return space;
  }

  public void setSpace(int space) {

    this.space = space;
  }
}
