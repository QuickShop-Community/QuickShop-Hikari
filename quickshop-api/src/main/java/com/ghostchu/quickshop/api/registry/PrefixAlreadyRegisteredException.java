package com.ghostchu.quickshop.api.registry;

import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.bukkit.plugin.Plugin;

public class PrefixAlreadyRegisteredException extends Exception {
    private final String prefix;
    private final Plugin plugin;
    private final ItemExpressionHandler handler;

    public PrefixAlreadyRegisteredException(String prefix, Plugin pluginInstance, ItemExpressionHandler handler) {
        super("The prefix " + prefix + " already in use, registered by " + pluginInstance.getName() + " with handler " + handler.getClass().getName() + ", pick another one prefix!");
        this.prefix = prefix;
        this.plugin = pluginInstance;
        this.handler = handler;
    }

    public String getPrefix() {
        return prefix;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public ItemExpressionHandler getHandler() {
        return handler;
    }
}
