package com.ghostchu.quickshop.api.event.details;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;

/**
 * Fire when quickshop processing the shop owner name
 */
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {

  private final Shop shop;
  private final QUser owner;
  private Component name;

  public ShopOwnerNameGettingEvent(final Shop shop, final QUser owner, final Component name) {

    this.shop = shop;
    this.owner = owner;
    this.name = name;
  }

  /**
   * Getting the shop owner display name
   *
   * @return The shop owner display name
   */
  public Component getName() {

    return name;
  }

  /**
   * Sets the shop owner display name
   *
   * @param name New shop owner display name, just display, won't change actual shop owner
   */
  public void setName(final Component name) {

    this.name = name;
  }

  /**
   * Getting the shop owner unique id
   *
   * @return The shop owner unique id
   */
  public QUser getOwner() {

    return owner;
  }

  /**
   * Getting the shop that trying getting the shop owner name
   *
   * @return The shop
   */
  public Shop getShop() {

    return shop;
  }
}
