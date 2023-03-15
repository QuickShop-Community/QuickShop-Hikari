package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

public class SubCommand_RemoveAll implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_RemoveAll(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            //copy it first
            List<Shop> tempList = new ArrayList<>(plugin.getShopManager().getAllShops());
            OfflinePlayer shopOwner = null;
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() != null && player.getName().equalsIgnoreCase(parser.getArgs().get(0))) {
                    shopOwner = player;
                    break;
                }
            }
            if (shopOwner == null) {
                plugin.text().of(sender, "unknown-player").send();
                return;
            }

            int i = 0;
            if (!shopOwner.equals(sender)) { //Non-self shop
                if (!plugin.perm().hasPermission(sender, "quickshop.removeall.other")) {
                    plugin.text().of(sender, "no-permission").send();
                    return;
                }
                for (Shop shop : tempList) {
                    if (shop.getOwner().equals(shopOwner.getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "Deleting shop " + shop + " as requested by the /qs removeall command.", shop.saveToInfoStorage()));
                        shop.delete();
                        i++;
                    }
                }
            } else { //Self shop
                if (!plugin.perm().hasPermission(sender, "quickshop.removeall.self")) {
                    plugin.text().of(sender, "no-permission").send();
                    return;
                }
                if (!(sender instanceof OfflinePlayer)) {
                    sender.sendMessage(ChatColor.RED + "This command can't be run by the console!");
                    return;
                }
                for (Shop shop : tempList) {
                    if (shop.getOwner().equals(((OfflinePlayer) sender).getUniqueId())) {
                        plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "Deleting shop " + shop + " as requested by the /qs removeall command.", shop.saveToInfoStorage()));
                        shop.delete();
                        i++;
                    }
                }
            }
            plugin.text().of(sender, "command.some-shops-removed", i).send();
        } else {
            plugin.text().of(sender, "command.no-owner-given").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() <= 1 ? getPlayerList() : Collections.emptyList();
    }
}
