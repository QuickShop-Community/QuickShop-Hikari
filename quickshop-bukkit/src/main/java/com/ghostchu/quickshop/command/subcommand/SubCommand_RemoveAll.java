package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

public class SubCommand_RemoveAll implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_RemoveAll(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        CompletableFuture<QUser> qUserFuture;
        if (parser.getArgs().size() == 1) {
            qUserFuture = QUserImpl.createAsync(plugin.getPlayerFinder(), parser.getArgs().get(0));
        } else {
            qUserFuture = QUserImpl.createAsync(plugin.getPlayerFinder(), sender);
        }
        qUserFuture
                .thenAccept(qUser -> {
                    QUser executor = QUserImpl.createAsync(plugin.getPlayerFinder(), sender).join();
                    if (executor.equals(qUser)) {
                        if (!plugin.perm().hasPermission(sender, "quickshop.removeall.self")) {
                            plugin.text().of(sender, "no-permission").send();
                            return;
                        }
                    } else {
                        if (!plugin.perm().hasPermission(sender, "quickshop.removeall.other")) {
                            plugin.text().of(sender, "no-permission").send();
                            return;
                        }
                    }
                    List<Shop> pendingRemoval = new ArrayList<>();
                    for (Shop shop : plugin.getShopManager().getAllShops()) {
                        if (!shop.getOwner().equals(qUser)) continue;
                        pendingRemoval.add(shop);
                    }
                    Util.mainThreadRun(() -> {
                        pendingRemoval.forEach(shop -> {
                            plugin.logEvent(new ShopRemoveLog(qUser, "Deleting shop " + shop + " as requested by the /quickshop  removeall command.", shop.saveToInfoStorage()));
                            plugin.getShopManager().deleteShop(shop);
                        });
                        plugin.text().of(sender, "command.some-shops-removed", pendingRemoval.size()).send();
                    });

                })
                .exceptionally(err -> {
                    plugin.text().of(sender, "internal-error", err.getMessage()).send();
                    return null;
                });

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() <= 1 ? getPlayerList() : Collections.emptyList();
    }
}
