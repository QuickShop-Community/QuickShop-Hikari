package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
            plugin.text().of("not-looking-at-shop").send();
            return;
        }

        if (cmdArg.length < 3) {
            plugin.text().of("command-incorrect", "/qs permission <type> <operation> <target> [group]").send();
            return;
        }

        String type = cmdArg[0].toLowerCase(Locale.ROOT);
        String operation = cmdArg[1].toLowerCase(Locale.ROOT);
        String target = cmdArg[2].toLowerCase(Locale.ROOT);
        String group = cmdArg.length > 3 ? cmdArg[3] : null;
        ChatSheetPrinter sheet = new ChatSheetPrinter(sender);
        //noinspection SwitchStatementWithTooFewBranches
        switch (type) {
            case "user" -> {
                Profile profile = plugin.getPlayerFinder().find(target);
                if (profile == null) {
                    plugin.text().of("unknown-player", target).send();
                    return;
                }
                switch (operation) {
                    case "set" -> {
                        if (group == null || !plugin.getShopPermissionManager().hasGroup(group)) {
                            plugin.text().of("invalid-group", target).send();
                            return;
                        }
                        shop.setPlayerGroup(profile.getUniqueId(), group);
                        plugin.text().of("successfully-set-player-group", group).send();
                    }
                    case "list" -> {
                        sheet.printHeader();
                        plugin.text().of("permission.header-player").send();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            for (Map.Entry<UUID, String> map : shop.getPermissionAudiences().entrySet()) {
                                String name;
                                Profile s = plugin.getPlayerFinder().find(map.getKey());
                                if (s == null) {
                                    name = "unknown";
                                } else {
                                    name = s.getName();
                                }
                                plugin.text().of("permission.table", name, map.getValue()).send();
                            }
                            sheet.printFooter();
                        });
                    }
                }
            }
//            case "group" -> {
//                if(!plugin.getShopPermissionManager().hasGroup(target)){
//                    plugin.text().of("invalid-group",target).send();
//                    return;
//                }
//                //noinspection SwitchStatementWithTooFewBranches
//                switch (operation) {
//                    case "list" -> {
//                        sheet.printHeader();
//                        plugin.text().of("permission.header-group").send();
//
//                    }
//                }
//            }
            default -> plugin.text().of("bad-command-usage-detailed", "user").send();
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
        if (cmdArg.length == 0) {
            return ImmutableList.of("user", "group");
        }
        if (cmdArg.length == 1) {
            return ImmutableList.of("add", "remove", "list");
        }
        return null;
    }
}
