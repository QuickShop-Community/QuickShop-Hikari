package com.ghostchu.quickshop.addon.list.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SubCommand_List implements CommandHandler<Player> {
    private final QuickShop quickshop;

    public SubCommand_List(QuickShop quickshop) {
        this.quickshop = quickshop;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            lookupSelf(sender);
            return;
        }
        lookupOther(sender, cmdArg[0]);
    }

    private void lookupSelf(Player sender) {
        if (!sender.hasPermission("quickshopaddon.list.self")) {
            quickshop.text().of(sender, "no-permission").send();
            return;
        }
        lookup(sender, sender.getUniqueId());
    }

    private void lookupOther(@NotNull Player sender, @NotNull String userName) {
        if (!sender.hasPermission("quickshopaddon.list.other")) {
            quickshop.text().of(sender, "no-permission").send();
            return;
        }
        UUID targetUser = quickshop.getPlayerFinder().name2Uuid(userName);
        lookup(sender, targetUser);
    }

    private void lookup(@NotNull Player sender, @NotNull UUID lookupUser) {
        String name = quickshop.getPlayerFinder().uuid2Name(lookupUser);
        if (StringUtils.isEmpty(name)) {
            name = "Unknown";
        }
        List<Shop> shops = quickshop.getShopManager().getPlayerAllShops(lookupUser);
        ChatSheetPrinter printer = new ChatSheetPrinter(sender);
        printer.printHeader();
        printer.printLine(quickshop.text().of(sender, "addon.list.table-prefix", name, shops.size()).forLocale());
        int counter = 1;
        for (Shop shop : shops) {
            String shopName = shop.getShopName();
            Location location = shop.getLocation();
            String combineLocation = location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
            if (StringUtils.isEmpty(shopName)) {
                shopName = combineLocation;
            }
            Component shopTypeComponent;
            if (shop.isBuying()) {
                shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-buying").forLocale();
            } else {
                shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-selling").forLocale();
            }
            printer.printLine(quickshop.text().of(sender, "addon.list.entry", counter, shopName, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), quickshop.getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency()), shop.getShopStackingAmount(), shopTypeComponent).forLocale());
            counter++;
        }
        printer.printFooter();
    }
}
