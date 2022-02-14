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

package com.ghostchu.quickshop.compatibility.nocheatplus;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.ghostchu.quickshop.api.event.ProtectionCheckStatus;
import com.ghostchu.quickshop.api.event.ShopProtectionCheckEvent;

public final class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("QuickShop Compatibility Module - NoCheatPlus loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(ignoreCancelled = true)
    public void onFakeEventBegin(ShopProtectionCheckEvent event) {
        if (event.getStatus() == ProtectionCheckStatus.BEGIN)
            NCPExemptionManager.exemptPermanently(event.getPlayer().getUniqueId());
        else if(event.getStatus() == ProtectionCheckStatus.END)
            NCPExemptionManager.unexempt(event.getPlayer().getUniqueId());
    }
}
