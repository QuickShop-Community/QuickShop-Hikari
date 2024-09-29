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
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

public class SubCommand_List implements CommandHandler<Player> {

  private final QuickShop quickshop;

  private final int pageSize = 10;

  public SubCommand_List(final QuickShop quickshop) {

    this.quickshop = quickshop;
  }

  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    int page = 1;
    if(parser.getArgs().isEmpty()) {
      lookupSelf(sender, page);
      return;
    }
    if(!StringUtils.isNumeric(parser.getArgs().get(0))) {
      if(parser.getArgs().size() >= 2) {
        if(!StringUtils.isNumeric(parser.getArgs().get(1))) {
          quickshop.text().of(sender, "not-a-number", parser.getArgs().get(1)).send();
          return;
        }
        page = Integer.parseInt(parser.getArgs().get(2));
      }
      lookupOther(sender, parser.getArgs().get(0), page);
    } else {
      page = Integer.parseInt(parser.getArgs().get(0));
      lookupSelf(sender, page);
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      if(quickshop.perm().hasPermission(sender, "quickshopaddon.list.other")) {
        return getPlayerList();
      }
    }
    if(parser.getArgs().size() == 2) {
      return List.of("[<page>]");
    }
    return Collections.emptyList();
  }

  private void lookupSelf(final Player sender, final int page) {

    if(!quickshop.perm().hasPermission(sender, "quickshopaddon.list.self")) {
      quickshop.text().of(sender, "no-permission").send();
      return;
    }
    lookup(sender, sender.getUniqueId(), page);
  }

  private void lookupOther(@NotNull final Player sender, @NotNull final String userName, final int page) {

    if(!quickshop.perm().hasPermission(sender, "quickshopaddon.list.other")) {
      quickshop.text().of(sender, "no-permission").send();
      return;
    }
    final UUID targetUser = quickshop.getPlayerFinder().name2Uuid(userName);
    lookup(sender, targetUser, page);
  }

  private void lookup(@NotNull final Player sender, @NotNull final UUID lookupUser, final int page) {

    String name = quickshop.getPlayerFinder().uuid2Name(lookupUser);
    if(StringUtils.isEmpty(name)) {
      name = "Unknown";
    }
    final List<Shop> shops = quickshop.getShopManager().getAllShops(lookupUser);
    final ChatSheetPrinter printer = new ChatSheetPrinter(sender);

    final int startPos = (page - 1) * pageSize;
    int counter = 0;
    int loopCounter = 0;
    printer.printHeader();
    printer.printLine(quickshop.text().of(sender, "addon.list.table-prefix-pageable", name, page, (int)Math.ceil((double)shops.size() / pageSize)).forLocale());
    for(final Shop shop : shops) {
      counter++;
      if(counter < startPos) {
        continue;
      }
      String shopName = shop.getShopName();
      final Location location = shop.getLocation();
      final String combineLocation = location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
      if(StringUtils.isEmpty(shopName)) {
        shopName = combineLocation;
      }
      final Component shopNameComponent = LegacyComponentSerializer.legacySection().deserialize(shopName).append(Component.textOfChildren(Component.text(" (").append(Util.getItemStackName(shop.getItem())).append(Component.text(")"))).color(NamedTextColor.GRAY));
      final Component shopTypeComponent;
      if(shop.isBuying()) {
        shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-buying").forLocale();
      } else {
        shopTypeComponent = quickshop.text().of(sender, "menu.this-shop-is-selling").forLocale();
      }
      Component component = quickshop.text().of(sender, "addon.list.entry", counter, shopNameComponent, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), quickshop.getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency()), shop.getShopStackingAmount(), Util.getItemStackName(shop.getItem()), shopTypeComponent).forLocale();
      component = component.clickEvent(ClickEvent.runCommand("/quickshop silentpreview " + shop.getRuntimeRandomUniqueId()));
      printer.printLine(component);
      loopCounter++;
      if(loopCounter >= pageSize) {
        break;
      }
    }
    printer.printFooter();

  }
}
