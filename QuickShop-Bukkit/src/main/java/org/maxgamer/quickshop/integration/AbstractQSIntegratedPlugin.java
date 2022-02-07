/*
 * This file is a part of project QuickShop, the name is QSIntegratedPlugin.java
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

package org.maxgamer.quickshop.integration;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.integration.IntegratedPlugin;
import org.maxgamer.quickshop.api.integration.IntegrationStage;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;


@IntegrationStage
public abstract class AbstractQSIntegratedPlugin extends QuickShopInstanceHolder implements IntegratedPlugin, Listener {

    public AbstractQSIntegratedPlugin(QuickShop plugin) {
        super(plugin);
    }

    public void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unregisterListener() {
        HandlerList.unregisterAll(this);
    }

}
