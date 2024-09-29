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
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.QuickShop.taskCache;

public class SubCommand_TransferAll implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_TransferAll(final QuickShop plugin) {

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
          final String name = parser.getArgs().get(0);
          plugin.getPlayerFinder().name2UuidFuture(name).whenComplete((uuid, throwable)->{
            if(uuid == null) {
              plugin.text().of(sender, "unknown-player").send();
              return;
            }
            final Player receiver = Bukkit.getPlayer(uuid);
            if(receiver == null) {
              plugin.text().of(sender, "player-offline", name).send();
              return;
            }
            if(sender.getUniqueId().equals(uuid)) {
              plugin.text().of(sender, "transfer-no-self", name).send();
              return;
            }
            final QUser senderQUser = QUserImpl.createFullFilled(sender);
            final QUser receiverQUser = QUserImpl.createFullFilled(receiver);

            final List<Shop> shopsToTransfer = plugin.getShopManager().getAllShops(senderQUser);
            final ShopUtil.PendingTransferTask task = new ShopUtil.PendingTransferTask(senderQUser, receiverQUser, shopsToTransfer);
            taskCache.put(uuid, task);
            plugin.text().of(sender, "transfer-sent", name).send();
            plugin.text().of(receiver, "transfer-request", sender.getName()).send();
            plugin.text().of(receiver, "transferall-ask", 60).send();
          });
        }
      }
    }
    if(parser.getArgs().size() == 2) {
      if(!plugin.perm().hasPermission(sender, "quickshop.transferall.other")) {
        plugin.text().of(sender, "no-permission").send();
        return;
      }
      Util.asyncThreadRun(()->{
        final QUser fromQUser = QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(), parser.getArgs().get(0));
        final QUser targetQUser = QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(), parser.getArgs().get(1));

        final Player fromPlayer = fromQUser.getUniqueIdIfRealPlayer().map(Bukkit::getPlayer).orElse(null);
        final Player targetPlayer = targetQUser.getUniqueIdIfRealPlayer().map(Bukkit::getPlayer).orElse(null);

        if(fromPlayer == null) {
          plugin.text().of(sender, "unknown-player", "fromPlayer").send();
          return;
        }
        if(targetPlayer == null) {
          plugin.text().of(sender, "unknown-player", "targetPlayer").send();
          return;
        }

        final List<Shop> shopList = plugin.getShopManager().getAllShops(fromQUser);
        final ShopUtil.PendingTransferTask task = new ShopUtil.PendingTransferTask(fromQUser, targetQUser, shopList);
        Util.mainThreadRun(()->{
          task.commit(false);
          plugin.text().of(sender, "command.transfer-success-other", shopList.size(), parser.getArgs().get(0), parser.getArgs().get(1)).send();
        });
      });

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
