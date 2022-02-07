/*
 * This file is a part of project QuickShop, the name is QSListener.java
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

package org.maxgamer.quickshop.listener;

import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;

public abstract class AbstractQSListener implements Listener, Reloadable {
    protected final QuickShop plugin;

    public AbstractQSListener(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
