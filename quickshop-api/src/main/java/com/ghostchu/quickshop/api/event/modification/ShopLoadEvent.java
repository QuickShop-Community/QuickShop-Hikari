package com.ghostchu.quickshop.api.event.modification;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Call when loading shop
 */
public class ShopLoadEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Calling when shop loading
   *
   * @param shop Target shop
   */
  public ShopLoadEvent(@NotNull final Shop shop) {

    this.shop = shop;
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
