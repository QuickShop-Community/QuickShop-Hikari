/*
 *  This file is a part of project QuickShop, the name is Openinv.java
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

package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.lishid.openinv.IOpenInv;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {
    public IOpenInv openInv;
    public OpenInvInventoryManager manager;
    @Override
    public void onLoad() {
        super.onLoad();
        openInv = (IOpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
        manager = new OpenInvInventoryManager(openInv, this);
        getApi().getInventoryWrapperRegistry().register(this, manager);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getApi().getCommandManager().registerCmd(CommandContainer.builder().prefix("echest").permission("quickshop.echest").description((locale)->LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("messages.description"))).executor(new OpenInvCommand(this)).build());
    }

    @Override
    public void init() {

    }

    public IOpenInv getOpenInv() {
        return openInv;
    }

    public OpenInvInventoryManager getManager() {
        return manager;
    }
}
