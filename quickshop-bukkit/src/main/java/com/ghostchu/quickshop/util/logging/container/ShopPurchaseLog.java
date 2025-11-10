package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
public class ShopPurchaseLog {

  private static int v = 2;
  private ShopInfoStorage shop;
  private IShopType type;
  private String trader;
  private String itemName;
  private String itemStack;
  private int amount;
  private double balance;
  private double tax;

  public ShopPurchaseLog(final ShopInfoStorage shop, final IShopType type, final QUser trader, final String itemName, final String itemStack, final int amount, final double balance, final double tax) {

    this.shop = shop;
    this.type = type;
    this.trader = trader.serialize();
    this.itemName = itemName;
    this.itemStack = itemStack;
    this.amount = amount;
    this.balance = balance;
    this.tax = tax;
  }
}
