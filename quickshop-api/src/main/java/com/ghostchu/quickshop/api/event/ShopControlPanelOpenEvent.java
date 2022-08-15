package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when control panel opened for
 */
public class ShopControlPanelOpenEvent extends AbstractQSEvent implements QSCancellable {
    private final Shop shop;
    private final CommandSender sender;
    private boolean cancelled = false;
    private @Nullable Component cancelReason;

    /**
     * Called before shop control panel message.
     *
     * @param shop   The shop bought from
     * @param sender The player which receiving shop control panel message
     */
    public ShopControlPanelOpenEvent(@NotNull Shop shop, @NotNull CommandSender sender) {
        this.shop = shop;
        this.sender = sender;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
    }

    @Override
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    /**
     * Get the sender that opened control panel
     *
     * @return The sender
     */
    public CommandSender getSender() {
        return this.sender;
    }

    /**
     * Gets the shop of control panel opened
     *
     * @return The shop
     */
    public Shop getShop() {
        return this.shop;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
