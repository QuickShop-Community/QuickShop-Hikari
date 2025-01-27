package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.shop.history.ShopHistory;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_RECORDS;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_SUMMARY;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.SHOPS_DATA;

public class SubCommand_History implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_History(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final List<Shop> shops = new ArrayList<>();
    if(parser.getArgs().isEmpty()) {
      if(!plugin.perm().hasPermission(sender, "quickshop.history")) {
        plugin.text().of(sender, "no-permission").send();
        return;
      }
      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }
      if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS) && !plugin.perm().hasPermission(sender, "quickshop.other.history")) {
        plugin.text().of(sender, "no-permission");
        return;
      }
      shops.add(shop);
    } else {
      switch(parser.getArgs().get(0).toLowerCase(Locale.ROOT)) {
        case "owned" -> {
          if(!plugin.perm().hasPermission(sender, "quickshop.history.owned")) {
            plugin.text().of(sender, "no-permission").send();
            return;
          }
          shops.addAll(plugin.getShopManager().getAllShops(sender.getUniqueId()));
        }
        case "accessible" -> {
          if(!plugin.perm().hasPermission(sender, "quickshop.history.accessible")) {
            plugin.text().of(sender, "no-permission").send();
            return;
          }
          shops.addAll(plugin.getShopManager().getAllShops()
                               .stream().filter(s->s.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS)).toList());
        }
        case "global" -> {
          if(!plugin.perm().hasPermission(sender, "quickshop.history.global")) {
            plugin.text().of(sender, "no-permission").send();
            return;
          }
          shops.addAll(plugin.getShopManager().getAllShops());
        }
        default -> {
          plugin.text().of(sender, "command-incorrect", "/quickshop history <owned/accessible/global/[leave empty]>").send();
          return;
        }
      }
    }
    final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
    MenuManager.instance().addViewer(viewer);

    final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(sender);

    Util.asyncThreadRun(()->{
      final ShopHistory shopHistory = new ShopHistory(QuickShop.getInstance(), shops);

      try {
        final List<ShopHistory.ShopHistoryRecord> queryResult = shopHistory.query();
        final ShopHistory.ShopSummary summary = shopHistory.generateSummary().join();
        Log.debug(summary.toString());

        if(queryResult == null) {
          return;
        }

        viewer.addData(SHOPS_DATA, shops);
        viewer.addData(HISTORY_RECORDS, queryResult);
        viewer.addData(HISTORY_SUMMARY, summary);
        Util.mainThreadRun(()->{
          MenuManager.instance().open("qs:history", 1, menuPlayer);
        });

      } catch(final Exception e) {
        MenuManager.instance().removeViewer(sender.getUniqueId());
        plugin.text().of(sender.getUniqueId(), "internal-error", sender.getUniqueId()).send();
        QuickShop.getInstance().logger().error("Couldn't query the shop history for shops {}.", shopHistory.shops(), e);
      }
    });
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("owned", "accessible", "global", plugin.text().of(sender, "history-command-leave-blank").plain());
    }
    return Collections.emptyList();
  }
}
