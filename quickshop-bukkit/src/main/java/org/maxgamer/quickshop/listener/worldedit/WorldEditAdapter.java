/*
 * This file is a part of project QuickShop, the name is WorldEditAdapter.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.listener.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.listener.AbstractQSListener;

public class WorldEditAdapter extends AbstractQSListener {
    private final WorldEditPlugin weBukkit;

    public WorldEditAdapter(QuickShop plugin, WorldEditPlugin weBukkit) {
        super(plugin);
        this.weBukkit = weBukkit;
    }

    @Override
    public void register() {
        weBukkit.getWorldEdit().getEventBus().register(this);
    }

    @Override
    public void unregister() {
        weBukkit.getWorldEdit().getEventBus().unregister(this);
    }

    @Subscribe(priority = EventHandler.Priority.NORMAL)
    public void proxyEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        World world = event.getWorld();
        if (actor != null && event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
            event.setExtent(new WorldEditBlockListener(actor, world, event.getExtent(), plugin));
        }
    }


}
