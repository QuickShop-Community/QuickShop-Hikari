package com.ghostchu.quickshop.eventmanager;

import com.ghostchu.quickshop.api.eventmanager.QuickEventManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A simple impl for Bukkit original EventManager
 *
 * @author Ghost_chu
 */
public class BukkitEventManager implements QuickEventManager {
    @Override
    public void callEvent(@NotNull Event event, @Nullable Consumer<Event> callBeforePassToMonitor) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
