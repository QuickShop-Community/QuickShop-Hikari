package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop price was changed
 */
public class ShopPriceChangeEvent extends AbstractQSEvent implements QSCancellable {

  private final double newPrice;

  private final double oldPrice;

  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Will call when shop price was changed.
   *
   * @param shop     Target shop
   * @param oldPrice The old shop price
   * @param newPrice The new shop price
   */
  public ShopPriceChangeEvent(@NotNull final Shop shop, final double oldPrice, final double newPrice) {

    this.shop = shop;
    this.oldPrice = oldPrice;
    this.newPrice = newPrice;
  }

  @Override
  public @Nullable Component getCancelReason() {

    return this.cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.cancelReason = reason;
  }

  /**
   * Gets the price that shop will use
   *
   * @return new price
   */
  public double getNewPrice() {

    return this.newPrice;
  }

  /**
   * Gets the price that shop used before
   *
   * @return old price
   */
  public double getOldPrice() {

    return this.oldPrice;
  }

  /**
   * Gets the shop
   *
   * @return the shop
   */
  public @NotNull Shop getShop() {

    return this.shop;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
