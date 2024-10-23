package com.ghostchu.quickshop.api.shop.cache;

public interface ShopInventoryCountCache {

  /**
   * Gets the stock
   *
   * @return the stock, returns -1 for never calculated or unlimited shop returns -2 for records not
   * cached into database yet
   */
  int getStock();

  /**
   * Gets the space
   *
   * @return the space, returns -1 for never calculated or unlimited shop returns -2 for records not
   * cached into database yet
   */
  int getSpace();

  /**
   * Check if the cache has been initialized.
   *
   * @return true if the cache has been initialized, false otherwise
   */
  boolean initialized();

}