package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Parent about all events.
 */
public abstract class AbstractQSEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    protected AbstractQSEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    protected AbstractQSEvent(boolean async) {
        super(async);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Fire event on Bukkit event bus
     */
    public void callEvent() {
        QuickShopAPI.getPluginInstance().getServer().getPluginManager().callEvent(this);
    }

    /**
     * Call event on Bukkit event bus and check if cancelled
     *
     * @return Returns true if cancelled, and false if didn't cancel
     */
    public boolean callCancellableEvent() {
        Bukkit.getPluginManager().callEvent(this);
        if (this instanceof Cancellable) {
            return ((Cancellable) this).isCancelled();
        }
        return false;
    }

}
