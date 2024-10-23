package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop price was changed
 */
public class ShopTaxAccountChangeEvent extends AbstractQSEvent implements QSCancellable {

  @Nullable
  private final QUser newTaxAccount;
  @NotNull
  private final Shop shop;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Will call when shop price was changed.
   *
   * @param shop          Target shop
   * @param newTaxAccount The new shop tax account
   */
  public ShopTaxAccountChangeEvent(@NotNull final Shop shop, @Nullable final QUser newTaxAccount) {

    this.shop = shop;
    this.newTaxAccount = newTaxAccount;
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

  @Nullable
  public QUser getNewTaxAccount() {

    return newTaxAccount;
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
