/*
 *  This file is a part of project QuickShop, the name is Main.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.compatibility.clearlag;

import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
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
