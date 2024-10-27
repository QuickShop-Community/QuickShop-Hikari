package com.ghostchu.quickshop.api.event.details;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fire when a shop ownership was transferred.
 */
public class ShopOwnershipTransferEvent extends AbstractQSEvent implements QSCancellable {

  private final Shop shop;
  private final QUser oldOwner;
  private final QUser newOwner;
  private boolean cancelled;
  private Component reason;

  /**
   * Create a new ShopOwnershipTransferEvent.
   *
   * @param shop     The shop.
   * @param oldOwner The old owner.
   * @param newOwner The new owner.
   */
  public ShopOwnershipTransferEvent(@NotNull final Shop shop, @NotNull final QUser oldOwner, @NotNull final QUser newOwner) {

    this.shop = shop;
    this.oldOwner = oldOwner;
    this.newOwner = newOwner;
  }

  @Override
  public @Nullable Component getCancelReason() {

    return reason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.reason = reason;
  }

  /**
   * Gets the new owner will transfer to.
   *
   * @return The new owner.
   */
  @NotNull
  public QUser getNewOwner() {

    return newOwner;
  }

  /**
   * Gets the old owner that transfer from.
   *
   * @return The old owner.
   */
  @NotNull
  public QUser getOldOwner() {

    return oldOwner;
  }

  /**
   * Gets the shop related to this event.
   *
   * @return The shop.
   */
  @NotNull
  public Shop getShop() {

    return shop;
  }

  @Override
  public boolean isCancelled() {

    return cancelled;
  }
}
