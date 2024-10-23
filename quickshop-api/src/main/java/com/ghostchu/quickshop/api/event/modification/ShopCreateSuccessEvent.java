package com.ghostchu.quickshop.api.event.modification;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/**
 * Calling when new shop creating
 */
public class ShopCreateSuccessEvent extends AbstractQSEvent {

  @NotNull
  private final QUser creator;

  @NotNull
  private final Shop shop;


  /**
   * Call when have a new shop was created.
   *
   * @param shop    Target shop
   * @param creator The player creating the shop, the player might offline/not exist if creating by
   *                a plugin.
   */
  public ShopCreateSuccessEvent(@NotNull final Shop shop, @NotNull final QUser creator) {

    this.shop = shop;
    this.creator = creator;
  }


  /**
   * Gets the creator of this shop
   *
   * @return The creator, may be a online/offline/virtual player
   */
  public @NotNull QUser getCreator() {

    return this.creator;
  }


  /**
   * Gets the shop created
   *
   * @return The shop that created
   */
  public @NotNull Shop getShop() {

    return this.shop;
  }
}
