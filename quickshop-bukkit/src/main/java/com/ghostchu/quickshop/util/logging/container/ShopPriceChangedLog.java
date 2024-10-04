package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
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
}
