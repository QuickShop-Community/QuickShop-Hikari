package com.ghostchu.quickshop.shop.inventory;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface BukkitListenerDrivenInventoryListener {
    boolean notify(Location updated); // May call from async thread
}
