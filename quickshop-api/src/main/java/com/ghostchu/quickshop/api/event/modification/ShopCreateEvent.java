package com.ghostchu.quickshop.api.event.modification;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when new shop creating
 */
public class ShopCreateEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final QUser creator;

  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Call when have a new shop was creating.
   *
   * @param shop    Target shop
   * @param creator The player creating the shop, the player might offline/not exist if creating by
   *                a plugin.
   */
  public ShopCreateEvent(@NotNull final Shop shop, @NotNull final QUser creator) {

    this.shop = shop;
    this.creator = creator;
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

  @Override
  public boolean isCancelled() {

    return cancelled;
  }
}
