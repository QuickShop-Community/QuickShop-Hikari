package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when shop tax calculation
 */
public class ShopTaxEvent extends AbstractQSEvent {

  private final QUser user;
  private final Shop shop;
  private double tax;

  /**
   * Call when shop calc shop tax that will pay to system account and remove from shop owner/player
   * received money
   *
   * @param shop The shop
   * @param tax  The tax
   * @param user The user (buyer/seller)
   */
  public ShopTaxEvent(@NotNull final Shop shop, final double tax, @NotNull final QUser user) {

    this.shop = shop;
    this.tax = tax;
    this.user = user;
  }

  /**
   * Gets the shop
   *
   * @return the shop
   */
  public Shop getShop() {

    return this.shop;
  }

  /**
   * Gets the tax in purchase
   *
   * @return tax
   */
  public double getTax() {

    return this.tax;
  }

  /**
   * Sets the new tax in purchase
   *
   * @param tax New tax
   */
  public void setTax(final double tax) {

    this.tax = tax;
  }

  /**
   * Gets the user (buyer or seller)
   *
   * @return User
   */
  public QUser getUser() {

    return this.user;
  }
}
