/*
 * This file is a part of project QuickShop, the name is BukkitEventManager.java
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

package org.maxgamer.quickshop.eventmanager;

import org.bukkit.event.Event;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.eventmanager.QuickEventManager;

/**
 * A simple impl for Bukkit original EventManager
 *
 * @author Ghost_chu
 */
public class BukkitEventManager implements QuickEventManager {
    @Override
    public void callEvent(Event event) {
        QuickShop.getInstance().getServer().getPluginManager().callEvent(event);
    }
}
