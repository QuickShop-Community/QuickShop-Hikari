package com.ghostchu.quickshop.command.subcommand;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 QuickShop-Community
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.QuickShop.taskCache;

public class SubCommand_TransferOwnership implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_TransferOwnership(final QuickShop plugin) {

    this.plugin = plugin;
  }


  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.wrong-args").send();
      return;
    }

    if(parser.getArgs().size() == 1) {
      switch(parser.getArgs().get(0)) {
        case "accept", "allow", "yes" -> {
          final ShopUtil.PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
          taskCache.invalidate(sender.getUniqueId());
          if(task == null) {
            plugin.text().of(sender, "transfer-no-pending-operation").send();
            return;
          }
          task.commit(true);
        }
        case "reject", "deny", "no" -> {
          final ShopUtil.PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
          taskCache.invalidate(sender.getUniqueId());
          if(task == null) {
            plugin.text().of(sender, "transfer-no-pending-operation").send();
            return;
          }
          task.cancel(true);
        }
        default -> {
          final Shop targetShop = getLookingShop(sender);
          if(targetShop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
          }
          if(!targetShop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.OWNERSHIP_TRANSFER)) {
            plugin.text().of(sender, "no-permission").send();
            return;
          }
          final String name = parser.getArgs().get(0);
          plugin.getPlayerFinder().name2UuidFuture(name).whenComplete((uuid, throwable)->{
            if(uuid == null) {
              plugin.text().of(sender, "unknown-player").send();
              return;
            }
            ShopUtil.transferRequest(sender.getUniqueId(), uuid, name, targetShop);
          });
        }
      }
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final List<String> list = Util.getPlayerList();
    list.add("accept");
    list.add("deny");
    return parser.getArgs().size() <= 2? list : Collections.emptyList();
  }
}
