/*
 *  This file is a part of project QuickShop, the name is Worldedit.java
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

package com.ghostchu.quickshop.compatibility.worldedit;

import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {
    private WorldEditAdapter adapter;

    @Override
    public void onEnable() {
        // Plugin startup logic
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        adapter = new WorldEditAdapter(worldEditPlugin);
        Bukkit.getPluginManager().registerEvents(adapter, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        adapter.unregister();
        super.onDisable();
    }

    @Override
    public void init() {

    }

}
