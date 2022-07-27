package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
public class SubCommand_RemoveWorld implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.no-world-given").send();
            return;
        }
        World world = Bukkit.getWorld(cmdArg[0]);
        if (world == null) {
            plugin.text().of(sender, "world-not-exists", cmdArg[0]).send();
            return;
        }
        int shopsDeleted = 0;
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (Objects.equals(shop.getLocation().getWorld(), world)) {
                shop.delete();
                shopsDeleted++;
            }
        }
        Log.debug("Successfully deleted all shops in world " + cmdArg[0] + "!");
        plugin.text().of(sender, "shops-removed-in-world", shopsDeleted, world.getName()).send();
    }

}
