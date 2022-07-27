package com.ghostchu.quickshop.compatibility.clearlag;

import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onRemove(EntityRemoveEvent event) {
        for (Entity entity : event.getEntityList()) {
            if (entity instanceof Item item) {
                if (AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack())) {
                    event.removeEntity(entity);
                }
            }
        }
    }

    @Override
    public void init() {

    }
}
