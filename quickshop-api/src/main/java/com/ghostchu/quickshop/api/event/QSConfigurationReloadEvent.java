package com.ghostchu.quickshop.api.event;

import org.bukkit.plugin.Plugin;

/**
 * Fire when QuickShop configuration reloaded.
 */
public class QSConfigurationReloadEvent extends AbstractQSEvent {

    private final Plugin instance;

    /**
     * Called when Quickshop plugin reloaded
     *
     * @param instance Quickshop instance
     */
    public QSConfigurationReloadEvent(Plugin instance) {
        this.instance = instance;
    }

    public Plugin getInstance() {
        return instance;
    }
}
