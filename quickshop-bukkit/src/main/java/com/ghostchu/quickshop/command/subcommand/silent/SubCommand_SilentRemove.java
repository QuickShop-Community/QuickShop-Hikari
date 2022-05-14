/*
 *  This file is a part of project QuickShop, the name is SubCommand_SilentRemove.java
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
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_SilentRemove extends SubCommand_SilentBase {

    public SubCommand_SilentRemove(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(@NotNull Player sender, @NotNull Shop shop, @NotNull String[] cmdArg) {
        if (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.DELETE)
                && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.destroy")) {
            plugin.text().of(sender, "no-permission").send();
            return;
        }

        plugin.logEvent(new ShopRemoveLog(sender.getUniqueId(), "/qs silentremove command", shop.saveToInfoStorage()));
        shop.delete();
    }
}
