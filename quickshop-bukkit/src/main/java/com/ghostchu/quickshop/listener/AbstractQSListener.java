package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AbstractQSListener implements Listener, Reloadable {
    protected final QuickShop plugin;

    protected AbstractQSListener(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getJavaPlugin());
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
