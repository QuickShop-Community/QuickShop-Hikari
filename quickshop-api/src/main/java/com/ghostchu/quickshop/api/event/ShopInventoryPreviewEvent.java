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
    public ShopInventoryPreviewEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
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
}
