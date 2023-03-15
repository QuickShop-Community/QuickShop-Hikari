package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubCommand_Staff implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = List.of("add", "del", "list", "clear");

    public SubCommand_Staff(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null
                    || (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.MANAGEMENT_PERMISSION)
                    && !plugin.perm().hasPermission(sender, "quickshop.other.staff"))) {
                continue;
            }
            switch (parser.getArgs().size()) {
                case 1 -> {
                    switch (parser.getArgs().get(0)) {
                        case "clear" -> {
                            shop.playersCanAuthorize(BuiltInShopPermissionGroup.STAFF).forEach(staff -> shop.setPlayerGroup(staff, BuiltInShopPermissionGroup.EVERYONE));
                            plugin.text().of(sender, "shop-staff-cleared").send();
                            return;
                        }
                        case "list" -> {
                            final List<UUID> staffs = shop.playersCanAuthorize(BuiltInShopPermissionGroup.STAFF);
                            if (staffs.isEmpty()) {
                                MsgUtil.sendDirectMessage(sender, plugin.text().of(sender, "tableformat.left_begin").forLocale()
                                        .append(Component.text("Empty").color(NamedTextColor.GRAY)));
                                return;
                            }
                            Util.asyncThreadRun(() -> {
                                for (UUID uuid : staffs) {
                                    MsgUtil.sendDirectMessage(sender, plugin.text().of(sender, "tableformat.left_begin").forLocale()
                                            .append(Component.text(Optional.ofNullable(plugin.getPlayerFinder().uuid2Name(uuid)).orElse("Unknown")).color(NamedTextColor.GRAY)));
                                }
                            });

                            return;
                        }
                        default -> {
                            plugin.text().of(sender, "command.wrong-args").send();
                            return;
                        }
                    }
                }
                case 2 -> {
                    String name = parser.getArgs().get(1);
                    Util.asyncThreadRun(() -> {
                        UUID uuid = plugin.getPlayerFinder().name2Uuid(parser.getArgs().get(1));
                        Util.mainThreadRun(() -> {
                            switch (parser.getArgs().get(0)) {
                                case "add" -> {
                                    shop.setPlayerGroup(uuid, BuiltInShopPermissionGroup.STAFF);
                                    plugin.text().of(sender, "shop-staff-added", name).send();
                                    return;
                                }
                                case "del" -> {
                                    shop.setPlayerGroup(uuid, BuiltInShopPermissionGroup.EVERYONE);
                                    plugin.text().of(sender, "shop-staff-deleted", name).send();
                                    return;
                                }
                                default -> {
                                    plugin.text().of(sender, "command.wrong-args").send();
                                    return;
                                }
                            }
                        });
                    });
                }
                default -> {
                    plugin.text().of(sender, "command.wrong-args").send();
                    return;
                }
            }
        }
        //no match shop
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {

        if (parser.getArgs().size() == 1) {
            return tabCompleteList;
        } else if (parser.getArgs().size() == 2) {
            String prefix = parser.getArgs().get(0).toLowerCase();
            if ("add".equals(prefix) || "del".equals(parser.getArgs().get(0))) {
                return Util.getPlayerList();
            }
        }
        return Collections.emptyList();
    }
}
