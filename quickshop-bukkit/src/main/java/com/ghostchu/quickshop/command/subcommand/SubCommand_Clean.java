package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Clean implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    public SubCommand_Clean(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        plugin.text().of(sender, "command.cleaning").send();

        final List<Shop> pendingRemoval = new ArrayList<>();
        int i = 0;

        for (Shop shop : plugin.getShopManager().getAllShops()) {
            try {
                if (Util.isLoaded(shop.getLocation())
                        && shop.isSelling()
                        && shop.getRemainingStock() == 0) {
                    pendingRemoval.add(
                            shop); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
                    // Can be deleted safely.
                    i++;
                }
            } catch (IllegalStateException e) {
                pendingRemoval.add(shop); // The shop is not there anymore, remove it
            }
        }

        for (Shop shop : pendingRemoval) {
            plugin.logEvent(new ShopRemoveLog(Util.getSenderUniqueId(sender), "/qs clean", shop.saveToInfoStorage()));
            shop.delete();
        }

        MsgUtil.clean();
        plugin.text().of(sender, "command.cleaned", i).send();
    }


}
