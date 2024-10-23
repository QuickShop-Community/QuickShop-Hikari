package com.ghostchu.quickshop.api.event.display;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * This event called after QuickShop safe guarded (+protection flags and attributes) a display
 * item.
 */
public class ShopDisplayItemSafeGuardEvent extends AbstractQSEvent {

  @NotNull
  private final Shop shop;

  @NotNull
  private final Entity entity;

  /**
   * This event called after QuickShop safe guarded (+protection flags and attributes) a display
   * item.
   *
   * @param shop   the shop
   * @param entity the display item
   */
  public ShopDisplayItemSafeGuardEvent(@NotNull final Shop shop, @NotNull final Entity entity) {

    this.shop = shop;
    this.entity = entity;
  }

  /**
   * Gets the display item.
   *
   * @return the display item
   */
  @NotNull
  public Entity getEntity() {

    return entity;
  }

  /**
   * Gets the shop that the display item belongs to.
   *
   * @return the shop
   */
  @NotNull
  public Shop getShop() {

    return shop;
  }
}
