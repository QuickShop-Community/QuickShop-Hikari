package com.ghostchu.quickshop.eventmanager;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.eventmanager.QuickEventManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A simple impl for Bukkit original EventManager
 *
 * @author Ghost_chu
 */
public class BukkitEventManager implements QuickEventManager {
    @Override
    public void callEvent(@NotNull Event event) {
        QuickShop.getInstance().getServer().getPluginManager().callEvent(event);
    }
}
