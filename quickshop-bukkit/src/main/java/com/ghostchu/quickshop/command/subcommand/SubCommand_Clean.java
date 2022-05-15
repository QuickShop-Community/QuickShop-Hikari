/*
 *  This file is a part of project QuickShop, the name is SubCommand_Clean.java
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

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Clean implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        plugin.text().of(sender, "command.cleaning").send();

        final List<Shop> pendingRemoval = new ArrayList<>();
        int i = 0;

        for (Shop shop : plugin.getShopManager().getAllShops()) {
            try {
                if (Util.isLoaded(shop.getLocation())
                        && shop.isSelling()
                        && shop.getRemainingStock() == 0
                        && shop instanceof ContainerShop cs) {
                    if (cs.isDoubleShop()) {
                        continue;
                    }
                    pendingRemoval.add(
                            shop); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
                    // Can be deleted safely.
                    i++;
                }
            } catch (IllegalStateException e) {
                pendingRemoval.add(shop); // The shop is not there anymore, remove it
            }
        }

        for (Shop shop : pendingRemoval) {
            plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs clean", shop.saveToInfoStorage()));
            shop.delete();
        }

        MsgUtil.clean();
        plugin.text().of(sender, "command.cleaned", i).send();
    }


}
