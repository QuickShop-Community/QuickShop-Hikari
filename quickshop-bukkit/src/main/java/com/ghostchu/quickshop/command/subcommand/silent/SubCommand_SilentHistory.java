package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
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

import java.util.ArrayList;
import java.util.List;

import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_RECORDS;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.HISTORY_SUMMARY;
import static com.ghostchu.quickshop.menu.ShopHistoryMenu.SHOPS_DATA;

public class SubCommand_SilentHistory extends SubCommand_SilentBase {

  public SubCommand_SilentHistory(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS)
       && !plugin.perm().hasPermission(sender, "quickshop.other.history")) {
      plugin.text().of(sender, "not-managed-shop").send();
      return;
    }

    final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
    MenuManager.instance().addViewer(viewer);

    final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(sender);

    final List<Shop> shops = new ArrayList<>();
    shops.add(shop);
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
}
