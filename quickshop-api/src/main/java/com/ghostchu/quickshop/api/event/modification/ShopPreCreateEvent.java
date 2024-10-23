package com.ghostchu.quickshop.api.event.modification;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.obj.QUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called before the shop creation request is sent. E.g. A player clicks a chest, this
 * event is thrown, if successful, the player is asked how much they wish to trade for.
 */
public class ShopPreCreateEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final Location location;

  @NotNull
  private final QUser creator;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Calling when shop pre-creating. Shop won't one-percent will create after this event, if you
   * want get the shop created event, please use ShopCreateEvent
   *
   * @param creator  Target player
   * @param location The location will create be shop
   */
  public ShopPreCreateEvent(@NotNull final QUser creator, @NotNull final Location location) {

    this.location = location;
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
   * Gets the location that shop will created on
   *
   * @return The shop location
   */
  public @NotNull Location getLocation() {

    return this.location;
  }

  /**
   * Gets the creator
   *
   * @return creator
   */
  public @NotNull QUser getCreator() {

    return this.creator;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
