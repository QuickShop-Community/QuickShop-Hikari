package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SubCommand_Staff implements CommandHandler<Player> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = List.of("add", "del", "list", "clear");

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop == null
                    || (!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.MANAGEMENT_PERMISSION)
                    && !plugin.perm().hasPermission(sender, "quickshop.other.staff"))) {
                continue;
            }
            switch (cmdArg.length) {
                case 1:
                    switch (cmdArg[0]) {
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
                            for (UUID uuid : staffs) {
                                MsgUtil.sendDirectMessage(sender, plugin.text().of(sender, "tableformat.left_begin").forLocale()
                                        .append(Component.text(Bukkit.getOfflinePlayer(uuid).getName()).color(NamedTextColor.GRAY)));
                            }
                            return;
                        }
                        default -> {
                            plugin.text().of(sender, "command.wrong-args").send();
                            return;
                        }
                    }
                case 2:
                    Profile profile = plugin.getPlayerFinder().find(cmdArg[1]);
                    if (profile == null) {
                        plugin.text().of(sender, "unknown-player").send();
                        return;
                    }
                    switch (cmdArg[0]) {
                        case "add" -> {
                            shop.setPlayerGroup(profile.getUniqueId(), BuiltInShopPermissionGroup.STAFF);
                            plugin.text().of(sender, "shop-staff-added", profile.getName()).send();
                            return;
                        }
                        case "del" -> {
                            shop.setPlayerGroup(profile.getUniqueId(), BuiltInShopPermissionGroup.EVERYONE);
                            plugin.text().of(sender, "shop-staff-deleted", profile.getName()).send();
                            return;
                        }
                        default -> {
                            plugin.text().of(sender, "command.wrong-args").send();
                            return;
                        }
                    }
                default:
                    plugin.text().of(sender, "command.wrong-args").send();
                    return;
            }
        }
        //no match shop
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {

        if (cmdArg.length == 1) {
            return tabCompleteList;
        } else if (cmdArg.length == 2) {
            String prefix = cmdArg[0].toLowerCase();
            if ("add".equals(prefix) || "del".equals(cmdArg[0])) {
                return Util.getPlayerList();
            }
        }
        return Collections.emptyList();
    }
}
