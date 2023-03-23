package com.ghostchu.quickshop.addon.list.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            lookupSelf(sender);
            return;
        }
        lookupOther(sender, parser.getArgs().get(0));
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
            Component shopNameComponent = LegacyComponentSerializer.legacySection().deserialize(shopName).append(Component.textOfChildren(Component.text(" (").append(Util.getItemStackName(shop.getItem())).append(Component.text(")"))).color(NamedTextColor.GRAY));
            Component shopTypeComponent;
            if (shop.isBuying()) {
                shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-buying").forLocale();
            } else {
                shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-selling").forLocale();
            }
            Component component = quickshop.text().of(sender, "addon.list.entry", counter, shopNameComponent, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), quickshop.getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency()), shop.getShopStackingAmount(), Util.getItemStackName(shop.getItem()), shopTypeComponent).forLocale();
            component = component.clickEvent(ClickEvent.runCommand("/qs silentpreview " + shop.getRuntimeRandomUniqueId()));
            printer.printLine(component);
            counter++;
        }
        printer.printFooter();
    }
}
