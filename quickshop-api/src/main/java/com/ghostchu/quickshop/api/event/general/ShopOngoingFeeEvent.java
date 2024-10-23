package com.ghostchu.quickshop.api.event.general;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Fire when watcher processing the shop ongoing fee
 */
public class ShopOngoingFeeEvent extends AbstractQSEvent implements QSCancellable {

  private final QUser player;

  private final Shop shop;

  private double cost;
  private boolean cancelled;
  private @Nullable Component cancelReason;

  public ShopOngoingFeeEvent(final Shop shop, final QUser player, final double cost) {

    this.shop = shop;
    this.player = player;
    this.cost = cost;
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
   * Getting the cost in this event
   *
   * @return The ongoing fee
   */
  public double getCost() {

    return cost;
  }

  /**
   * Sets the ongoing fee to replace old one
   *
   * @param cost The ongoing fee
   */
  public void setCost(final double cost) {

    this.cost = cost;
  }

  /**
   * Getting related player in this event
   *
   * @return The player triggered ongoing fee event
   */
  public QUser getPlayer() {

    return player;
  }

  /**
   * Getting related shop in this event
   *
   * @return The shop triggered ongoing fee event
   */
  public Shop getShop() {

    return shop;
  }

  @Override
  public boolean isCancelled() {

    return cancelled;
  }
}
