package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.ShopOwnershipTransferEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
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

public class SubCommand_TransferOwnership implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final Cache<UUID, PendingTransferTask> taskCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    public SubCommand_TransferOwnership(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().isEmpty()) {
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
                    Shop targetShop = getLookingShop(sender);
                    if(targetShop == null){
                        plugin.text().of(sender, "not-looking-at-shop").send();
                        return;
                    }
                    if(!targetShop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.OWNERSHIP_TRANSFER)){
                        plugin.text().of(sender, "no-permission").send();
                        return;
                    }
                    String name = parser.getArgs().get(0);
                    plugin.getPlayerFinder().name2UuidFuture(name).whenComplete((uuid, throwable) -> {
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
                        QUser senderQUser = QUserImpl.createFullFilled(sender);
                        QUser receiverQUser = QUserImpl.createFullFilled(receiver);

                        List<Shop> shopsToTransfer = List.of(targetShop);
                        PendingTransferTask task = new PendingTransferTask(senderQUser, receiverQUser, shopsToTransfer);
                        taskCache.put(uuid, task);
                        plugin.text().of(sender, "transfer-sent", name).send();
                        plugin.text().of(receiver, "transfer-single-request", sender.getName()).send();
                        plugin.text().of(receiver, "transfer-single-ask", 60).send();
                    });
                }
            }
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
        private final QUser from;
        private final QUser to;
        private final List<Shop> shops;

        public PendingTransferTask(QUser from, QUser to, List<Shop> shops) {
            this.from = from;
            this.to = to;
            this.shops = shops;
        }

        public void cancel(boolean sendMessage) {
            if (sendMessage) {
                QuickShop.getInstance().text().of(from, "transfer-rejected-fromside", to).send();
                QuickShop.getInstance().text().of(to, "transfer-rejected-toside", from).send();
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
                QuickShop.getInstance().text().of(from, "transfer-accepted-fromside", to).send();
                QuickShop.getInstance().text().of(to, "transfer-accepted-toside", from).send();
            }
        }
    }
}
