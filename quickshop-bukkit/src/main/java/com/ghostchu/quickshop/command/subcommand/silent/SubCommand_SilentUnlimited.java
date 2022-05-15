/*
 *  This file is a part of project QuickShop, the name is SubCommand_SilentUnlimited.java
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

package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SubCommand_SilentUnlimited extends SubCommand_SilentBase {

    public SubCommand_SilentUnlimited(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull String[] cmdArg) {
        shop.setUnlimited(!shop.isUnlimited());
        shop.update();
        MsgUtil.sendControlPanelInfo(sender, shop);

        if (shop.isUnlimited()) {
            plugin.text().of(sender, "command.toggle-unlimited.unlimited").send();
            if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
                UUID uuid = ((SimpleShopManager) plugin.getShopManager()).getCacheUnlimitedShopAccount();
                Profile profile = plugin.getPlayerFinder().find(uuid);
                if (profile == null) {
                    Log.debug("Failed to migrate shop to unlimited shop owner, uniqueid invalid: " + uuid + ".");
                    return;
                }
                plugin.getShopManager().migrateOwnerToUnlimitedShopOwner(shop);
                plugin.text().of(sender, "unlimited-shop-owner-changed", profile.getName()).send();
            }
            return;
        }
        plugin.text().of(sender, "command.toggle-unlimited.limited").send();
        if (plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
            plugin.text().of(sender, "unlimited-shop-owner-keeped").send();
        }
    }

}
