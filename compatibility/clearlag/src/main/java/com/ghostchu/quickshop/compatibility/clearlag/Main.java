package com.ghostchu.quickshop.compatibility.clearlag;

import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;

public final class Main extends CompatibilityModule implements Listener {

    @Override
    public void init() {
        // There no init stuffs need to do
    }

    @EventHandler(ignoreCancelled = true)
    public void onRemove(EntityRemoveEvent event) {
        Iterator<Entity> entityListIt = event.getEntityList().iterator();
        while (entityListIt.hasNext()) {
            Entity entity = entityListIt.next();
            if (entity instanceof Item item) {
                if (AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack())) {
                    entityListIt.remove();
                }
            }
        }
    }
}
