package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SubCommand_Transfer implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final Cache<UUID, PendingTransferTask> taskCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    public SubCommand_Transfer(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "command.wrong-args").send();
            return;
        }
        if (parser.getArgs().size() == 1) {
            switch (parser.getArgs().get(0)) {
                case "accept", "allow", "yes" -> {
                    PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
                    taskCache.invalidate(sender.getUniqueId());
                    if (task == null) {
                        plugin.text().of(sender, "transfer-no-pending-operation").send();
                        return;
                    }
                    task.commit(true);
                }
                case "reject", "deny", "no" -> {
                    PendingTransferTask task = taskCache.getIfPresent(sender.getUniqueId());
                    taskCache.invalidate(sender.getUniqueId());
                    if (task == null) {
                        plugin.text().of(sender, "transfer-no-pending-operation").send();
                        return;
                    }
                    task.cancel(true);
                }
                default -> {
                    String name = parser.getArgs().get(0);
                    UUID uuid = plugin.getPlayerFinder().name2Uuid(name);
                    if (uuid == null) {
                        plugin.text().of(sender, "unknown-player").send();
                        return;
                    }
                    Player receiver = Bukkit.getPlayer(uuid);
                    if (receiver == null) {
                        plugin.text().of(sender, "player-offline", name).send();
                        return;
                    }
                    if (sender.getUniqueId().equals(uuid)) {
                        plugin.text().of(sender, "transfer-no-self", name).send();
                        return;
                    }
                    List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(sender.getUniqueId());
                    PendingTransferTask task = new PendingTransferTask(sender.getUniqueId(), uuid, shopList);
                    taskCache.put(uuid, task);
                    plugin.text().of(sender, "transfer-sent", name).send();
                    plugin.text().of(receiver, "transfer-request", sender.getName()).send();
                    plugin.text().of(receiver, "transfer-ask", 60).send();
                    return;
                }
            }
        }
        if (parser.getArgs().size() == 2) {
            if (!plugin.perm().hasPermission(sender, "quickshop.transfer.other")) {
                plugin.text().of(sender, "no-permission").send();
                return;
            }
            UUID fromPlayer = plugin.getPlayerFinder().name2Uuid(parser.getArgs().get(0));
            UUID targetPlayer = plugin.getPlayerFinder().name2Uuid(parser.getArgs().get(1));
            if (fromPlayer == null) {
                plugin.text().of(sender, "unknown-player", "fromPlayer").send();
                return;
            }
            if (targetPlayer == null) {
                plugin.text().of(sender, "unknown-player", "targetPlayer").send();
                return;
            }
            List<Shop> shopList = plugin.getShopManager().getPlayerAllShops(fromPlayer);
            PendingTransferTask task = new PendingTransferTask(fromPlayer, targetPlayer, shopList);
            task.commit(false);
            plugin.text().of(sender, "command.transfer-success-other", shopList.size(), parser.getArgs().get(0), parser.getArgs().get(1)).send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        List<String> list = Util.getPlayerList();
        list.add("accept");
        list.add("deny");
        return parser.getArgs().size() <= 2 ? list : Collections.emptyList();
    }

    @Data
    static class PendingTransferTask {
        private final UUID from;
        private final UUID to;
        private final List<Shop> shops;

        public PendingTransferTask(UUID from, UUID to, List<Shop> shops) {
            this.from = from;
            this.to = to;
            this.shops = shops;
        }

        public void cancel(boolean sendMessage) {
            if (sendMessage) {
                Util.asyncThreadRun(() -> {
                    String fromPlayerProfile = QuickShop.getInstance().getPlayerFinder().uuid2Name(from);
                    String toPlayerProfile = QuickShop.getInstance().getPlayerFinder().uuid2Name(to);
                    Player fromPlayer = Bukkit.getPlayer(from);
                    Player toPlayer = Bukkit.getPlayer(to);
                    if (fromPlayer != null && toPlayerProfile != null) {
                        QuickShop.getInstance().text().of(fromPlayer, "transfer-rejected-fromside", toPlayerProfile).send();
                    }
                    if (toPlayer != null && fromPlayerProfile != null) {
                        QuickShop.getInstance().text().of(toPlayer, "transfer-rejected-toside", fromPlayerProfile).send();
                    }
                });

            }
        }

        public void commit(boolean sendMessage) {
            for (Shop shop : shops) {
                ShopOwnershipTransferEvent event = new ShopOwnershipTransferEvent(shop, shop.getOwner(), to);
                if (event.callCancellableEvent()) {
                    continue;
                }
                shop.setOwner(to);
            }
            if (sendMessage) {
                Util.asyncThreadRun(() -> {
                    String fromPlayerProfile = QuickShop.getInstance().getPlayerFinder().uuid2Name(from);
                    String toPlayerProfile = QuickShop.getInstance().getPlayerFinder().uuid2Name(to);
                    if (toPlayerProfile != null) {
                        QuickShop.getInstance().text().of(from, "transfer-accepted-fromside", toPlayerProfile).send();
                    }
                    if (fromPlayerProfile != null) {
                        QuickShop.getInstance().text().of(to, "transfer-accepted-toside", fromPlayerProfile).send();
                    }
                });
            }
        }
    }
}
