package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@AllArgsConstructor
public class SubCommand_Permission implements CommandHandler<Player> {
    private final QuickShop plugin;

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone will receive stone)
     */
    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        String type = null;
        if (cmdArg.length > 0)
            type = cmdArg[0].toLowerCase(Locale.ROOT);
        String operation = null;
        if (cmdArg.length > 1)
            operation = cmdArg[1].toLowerCase(Locale.ROOT);
        String target = null;
        if (cmdArg.length > 2)
            target = cmdArg[2];
        String group = null;
        if (cmdArg.length > 3)
            group = cmdArg[3];
        ChatSheetPrinter sheet = new ChatSheetPrinter(sender);
        if (type == null) {
            plugin.text().of(sender, "bad-command-usage-detailed", "user,group").send();
            return;
        }
        switch (type) {
            case "user" -> {
                if (target == null) {
                    plugin.text().of(sender, "bad-command-usage-detailed", "set,list,unset").send();
                    return;
                }
                Profile profile = plugin.getPlayerFinder().find(target);
                if (profile == null) {
                    plugin.text().of(sender, "unknown-player", target).send();
                    return;
                }

                switch (operation) {
                    case "set" -> {
                        if (group == null) {
                            plugin.text().of(sender, "command-incorrect", "/qs permission user set <group>").send();
                            return;
                        }
                        if (!plugin.getShopPermissionManager().hasGroup(group)) {
                            plugin.text().of(sender, "invalid-group", target).send();
                            return;
                        }
                        shop.setPlayerGroup(profile.getUniqueId(), group);
                        plugin.text().of(sender, "successfully-set-player-group", profile.getName(), group).send();
                    }
                    case "unset" -> {
                        shop.setPlayerGroup(profile.getUniqueId(), BuiltInShopPermissionGroup.EVERYONE);
                        plugin.text().of(sender, "successfully-unset-player-group", profile.getName()).send();
                    }
                }
            }
            case "group" -> {
//                if (target == null) {
//                    plugin.text().of(sender, "bad-command-usage-detailed", "list").send();
//                    return;
//                }
//                if (!plugin.getShopPermissionManager().hasGroup(target)) {
//                    plugin.text().of(sender,"invalid-group", target).send();
//                    return;
//                }
                if (operation == null) {
                    plugin.text().of(sender, "bad-command-usage-detailed", "list").send();
                    return;
                }
                //noinspection SwitchStatementWithTooFewBranches
                switch (operation) {
                    case "list" -> {
                        sheet.printHeader();
                        sheet.printLine(plugin.text().of(sender, "permission.header").forLocale());
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            for (Map.Entry<UUID, String> map : shop.getPermissionAudiences().entrySet()) {
                                String name;
                                Profile s = plugin.getPlayerFinder().find(map.getKey());
                                if (s == null) {
                                    name = "unknown";
                                } else {
                                    name = s.getName();
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
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone [TAB] will receive stone)
     * @return Candidate list
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            return ImmutableList.of("user", "group");
        }
        if (cmdArg.length == 2) {
            if (cmdArg[0].equalsIgnoreCase("user")) {
                return ImmutableList.of("set", "unset");
            } else if (cmdArg[0].equalsIgnoreCase("group")) {
                return ImmutableList.of("list");
            }
        }
        if (cmdArg.length == 3) {
            if (cmdArg[0].equalsIgnoreCase("user")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (cmdArg.length == 4) {
            if (cmdArg[0].equalsIgnoreCase("user")) {
                return plugin.getShopPermissionManager().getGroups();
            }
        }
        return Collections.emptyList();
    }
}
