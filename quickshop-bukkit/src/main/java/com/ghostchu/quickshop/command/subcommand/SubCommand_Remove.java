package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Remove implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Remove(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    if(shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.DELETE)
       || plugin.perm().hasPermission(sender, "quickshop.other.destroy")) {
      plugin.getShopManager().deleteShop(shop);
      plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(sender), "/quickshop remove command", shop.saveToInfoStorage()));
    } else {
      plugin.text().of(sender, "no-permission").send();
    }
  }

}
