package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Clean implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_Clean(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    plugin.text().of(sender, "command.cleaning").send();

    final List<Shop> pendingRemoval = new ArrayList<>();
    int i = 0;

    for(final Shop shop : plugin.getShopManager().getAllShops()) {
      try {
        if(Util.isLoaded(shop.getLocation())
           && shop.isSelling()
           && shop.getRemainingStock() == 0) {
          pendingRemoval.add(
                  shop); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
          // Can be deleted safely.
          i++;
        }
        if(plugin.getShopItemBlackList().isBlacklisted(shop.getItem())) {
          pendingRemoval.add(shop);
          i++;
        }
      } catch(IllegalStateException e) {
        pendingRemoval.add(shop); // The shop is not there anymore, remove it
      }
    }

    for(final Shop shop : pendingRemoval) {
      plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop clean", shop.saveToInfoStorage()));
      plugin.getShopManager().deleteShop(shop);
    }

    MsgUtil.clean();
    plugin.text().of(sender, "command.cleaned", i).send();
  }

}
