package com.ghostchu.quickshop.api.eventmanager;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * QuickEventManager allow user switch between bukkit and quickshop.
 * QuickShop's EventManager can filter the Plugin listeners and skip it when calling events
 */
public interface QuickEventManager {
    /**
     * Calling an event use QuickShopEventManager
     *
     * @param event The event
     * @throws IllegalStateException Just like bukkit
     */
    void callEvent(@NotNull Event event) throws IllegalStateException;
}
