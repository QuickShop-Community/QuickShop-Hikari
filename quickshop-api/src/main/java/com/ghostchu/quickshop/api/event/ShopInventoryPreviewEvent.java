package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when previewing a Shop item
 */
public class ShopInventoryPreviewEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final ItemStack itemStack;

  @NotNull
  private final Player player;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Called when player using GUI preview
   *
   * @param player    Target plugin
   * @param itemStack The preview item, with preview flag.
   */
  public ShopInventoryPreviewEvent(@NotNull final Player player, @NotNull final ItemStack itemStack) {

    this.player = player;
    this.itemStack = itemStack;
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
   * Gets the ItemStack that previewing
   *
   * @return The ItemStack
   */
  public @NotNull ItemStack getItemStack() {

    return this.itemStack;
  }

  /**
   * Gets the Player that open gui for
   *
   * @return The player
   */
  public @NotNull Player getPlayer() {

    return this.player;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
