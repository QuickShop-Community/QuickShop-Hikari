package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;

import java.util.Objects;

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

  public ShopInfoStorage getShop() {

    return this.shop;
  }

  public IShopType getType() {

    return this.type;
  }

  public String getTrader() {

    return this.trader;
  }

  public String getItemName() {

    return this.itemName;
  }

  public String getItemStack() {

    return this.itemStack;
  }

  public int getAmount() {

    return this.amount;
  }

  public double getBalance() {

    return this.balance;
  }

  public double getTax() {

    return this.tax;
  }

  public void setShop(final ShopInfoStorage shop) {

    this.shop = shop;
  }

  public void setType(final IShopType type) {

    this.type = type;
  }

  public void setTrader(final String trader) {

    this.trader = trader;
  }

  public void setItemName(final String itemName) {

    this.itemName = itemName;
  }

  public void setItemStack(final String itemStack) {

    this.itemStack = itemStack;
  }

  public void setAmount(final int amount) {

    this.amount = amount;
  }

  public void setBalance(final double balance) {

    this.balance = balance;
  }

  public void setTax(final double tax) {

    this.tax = tax;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopPurchaseLog)) return false;
    final ShopPurchaseLog other = (ShopPurchaseLog)o;
    return this.getAmount() == other.getAmount()
           && Double.compare(this.getBalance(), other.getBalance()) == 0
           && Double.compare(this.getTax(), other.getTax()) == 0
           && Objects.equals(this.getShop(), other.getShop())
           && Objects.equals(this.getType(), other.getType())
           && Objects.equals(this.getTrader(), other.getTrader())
           && Objects.equals(this.getItemName(), other.getItemName())
           && Objects.equals(this.getItemStack(), other.getItemStack());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getAmount(), this.getBalance(), this.getTax(), this.getShop(), this.getType(), this.getTrader(), this.getItemName(), this.getItemStack());
  }

  @Override
  public String toString() {

    return "ShopPurchaseLog(shop=" + this.getShop() + ", type=" + this.getType() + ", trader=" + this.getTrader() + ", itemName=" + this.getItemName() + ", itemStack=" + this.getItemStack() + ", amount=" + this.getAmount() + ", balance=" + this.getBalance() + ", tax=" + this.getTax() + ")";
  }
}
