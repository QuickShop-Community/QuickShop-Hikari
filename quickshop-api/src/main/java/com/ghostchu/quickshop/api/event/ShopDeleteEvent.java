package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop deleting
 */
public class ShopDeleteEvent extends AbstractQSEvent implements QSCancellable {

  private final boolean fromMemory;

  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Call the event when shop is deleting. The ShopUnloadEvent will call after ShopDeleteEvent
   *
   * @param shop       Target shop
   * @param fromMemory Only delete from the memory? false = delete both in memory and database
   */
  public ShopDeleteEvent(@NotNull final Shop shop, final boolean fromMemory) {

    this.shop = shop;
    this.fromMemory = fromMemory;
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
   * Gets the shop that deleted
   *
   * @return The shop
   */
  public @NotNull Shop getShop() {

    return this.shop;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }

  /**
   * Gets the delete is from memory or also database
   *
   * @return only from memory
   */
  public boolean isFromMemory() {

    return this.fromMemory;
  }
}
