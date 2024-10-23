package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.Nullable;

/**
 * Fire when quickshop processing shop tax account.
 */
public class ShopTaxAccountGettingEvent extends AbstractQSEvent {

  private final Shop shop;
  @Nullable
  private QUser taxAccount;

  public ShopTaxAccountGettingEvent(final Shop shop, @Nullable final QUser taxAccount) {

    this.shop = shop;
    this.taxAccount = taxAccount;
  }

  /**
   * Gets the shop
   *
   * @return The shop
   */
  public Shop getShop() {

    return shop;
  }

  /**
   * Getting the tax account
   *
   * @return The tax account, null if tax has been disabled
   */
  @Nullable
  public QUser getTaxAccount() {

    return taxAccount;
  }

  /**
   * Sets the tax account
   *
   * @param taxAccount The tax account
   */
  public void setTaxAccount(@Nullable final QUser taxAccount) {

    this.taxAccount = taxAccount;
  }
}
