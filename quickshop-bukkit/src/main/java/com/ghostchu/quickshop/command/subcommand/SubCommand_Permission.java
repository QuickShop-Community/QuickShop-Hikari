package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SubCommand_Permission implements CommandHandler<Player> {
    private final QuickShop plugin;

    public SubCommand_Permission(QuickShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/quickshop = qs, /shop = shop)
     * @param parser       The arguments (/quickshop create stone will receive stone)
     */
    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.MANAGEMENT_PERMISSION)
                && !plugin.perm().hasPermission(sender, "quickshop.permission.bypass")) {
            plugin.text().of(sender, "no-permission").send();
            return;
        }
        String type = null;
        if (!parser.getArgs().isEmpty()) {
            type = parser.getArgs().get(0).toLowerCase(Locale.ROOT);
        }
        String operation = null;
        if (parser.getArgs().size() > 1) {
            operation = parser.getArgs().get(1).toLowerCase(Locale.ROOT);
        }
        String target = null;
        if (parser.getArgs().size() > 2) {
            target = parser.getArgs().get(2);
        }
        String group = null;
        if (parser.getArgs().size() > 3) {
            group = parser.getArgs().get(3);
        }
        ChatSheetPrinter sheet = new ChatSheetPrinter(sender);
        if (type == null) {
            plugin.text().of(sender, "bad-command-usage-detailed", "user,group").send();
            return;
        }
        if (operation == null) {
            plugin.text().of(sender, "bad-command-usage-detailed", "list").send();
            return;
        }
        final String typeFinal = type;
        final String operationFinal = operation;
        final String targetFinal = target;
        final String groupFinal = group;

        switch (type) {
            case "user" -> {
                if (target == null) {
                    plugin.text().of(sender, "bad-command-usage-detailed", "set,list,unset").send();
                    return;
                }
                plugin.getPlayerFinder().name2UuidFuture(targetFinal).whenComplete((uuid, throwable) -> {
                    if (throwable != null) {
                        plugin.logger().warn("Failed to get uuid of player " + targetFinal, throwable);
                        return;
                    }
                    if (uuid == null) {
                        plugin.text().of(sender, "unknown-player", targetFinal).send();
                        return;
                    }
                    switch (operationFinal) {
                        case "set" -> {
                            if (groupFinal == null) {
                                plugin.text().of(sender, "command-incorrect", "/quickshop permission user set <group>").send();
                                return;
                            }
                            if (!plugin.getShopPermissionManager().hasGroup(groupFinal)) {
                                plugin.text().of(sender, "invalid-group", targetFinal).send();
                                return;
                            }
                            shop.setPlayerGroup(uuid, groupFinal);
                            plugin.text().of(sender, "successfully-set-player-group", targetFinal, groupFinal).send();
                        }
                        case "unset" -> {
                            shop.setPlayerGroup(uuid, BuiltInShopPermissionGroup.EVERYONE);
                            plugin.text().of(sender, "successfully-unset-player-group", targetFinal).send();
                        }
                    }
                });
            }
            case "group" -> {
                //noinspection SwitchStatementWithTooFewBranches
                switch (operation) {
                    case "list" -> {
                        sheet.printHeader();
                        sheet.printLine(plugin.text().of(sender, "permission.header").forLocale());
                        Util.asyncThreadRun(() -> {
                            for (Map.Entry<UUID, String> map : shop.getPermissionAudiences().entrySet()) {
                                String name = plugin.getPlayerFinder().uuid2Name(map.getKey());
                                if (name == null) {
                                    name = "unknown";
                                }
                                sheet.printLine(plugin.text().of(sender, "permission.table", name, map.getValue()).forLocale());
                            }
                            sheet.printFooter();
                        });
                    }
                }
            }
            default -> plugin.text().of(sender, "bad-command-usage-detailed", "list").send();
        }


    }

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/quickshop = qs, /shop = shop)
     * @param parser       The arguments (/quickshop create stone [TAB] will receive stone)
     * @return Candidate list
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            return List.of("user", "group");
        }
        if (parser.getArgs().size() == 2) {
            if ("user".equalsIgnoreCase(parser.getArgs().get(0))) {
                return List.of("set", "unset");
            } else if ("group".equalsIgnoreCase(parser.getArgs().get(0))) {
                return List.of("list");
            }
        }
        if (parser.getArgs().size() == 3) {
            if ("user".equalsIgnoreCase(parser.getArgs().get(0))) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (parser.getArgs().size() == 4) {
            if ("user".equalsIgnoreCase(parser.getArgs().get(0))) {
                return plugin.getShopPermissionManager().getGroups();
            }
        }
        return Collections.emptyList();
    }
}
