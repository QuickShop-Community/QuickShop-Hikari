package com.ghostchu.quickshop.shop.cache;

import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;

public class SimpleShopInventoryCountCache implements ShopInventoryCountCache {

  private int stock;
  private int space;
  private boolean initialized = false;

  public SimpleShopInventoryCountCache(final int stock, final int space, final boolean initialized) {

    this.stock = stock;
    this.space = space;
    this.initialized = initialized;
  }
  /**
   * Gets the stock
   *
   * @return the stock, returns -1 for never calculated or unlimited shop returns -2 for records not
   * cached into database yet
   */
  @Override
  public int getStock() {

    return stock;
  }

  public void setStock(final int stock) {

    this.stock = stock;
  }

  /**
   * Gets the space
   *
   * @return the space, returns -1 for never calculated or unlimited shop returns -2 for records not
   * cached into database yet
   */
  @Override
  public int getSpace() {

    return space;
  }

  /**
   * Check if the cache has been initialized.
   *
   * @return true if the cache has been initialized, false otherwise
   */
  @Override
  public boolean initialized() {

    return initialized;
  }

  public void setSpace(final int space) {

    this.space = space;
  }
}
