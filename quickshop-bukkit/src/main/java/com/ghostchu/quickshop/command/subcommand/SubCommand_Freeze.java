package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.shop.SimpleShopManager.ACTIVE_STATE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.BUYING_TYPE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.FROZEN_STATE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.FROZEN_TYPE;

public class SubCommand_Freeze implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Freeze(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Shop shop = getLookingShop(sender);
    if(shop != null) {
      if(shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOP_STATE)
         || plugin.perm().hasPermission(sender, "quickshop.other.freeze")) {

        if(!shop.shopState().isTradingAllowed() && shop.shopState().identifier().equalsIgnoreCase("FROZEN")) {

          shop.shopState(ACTIVE_STATE);
          plugin.text().of(sender, "shop-nolonger-freezed", Util.getItemStackName(shop.getItem())).send();

          if(shop.shopType().identifier().equalsIgnoreCase(BUYING_TYPE.identifier())) {

            plugin.text().of(sender, "command.now-buying", Util.getItemStackName(shop.getItem())).send();
          } else {

            plugin.text().of(sender, "command.now-selling", Util.getItemStackName(shop.getItem())).send();
          }
        } else {

          shop.shopState(FROZEN_STATE);
          plugin.text().of(sender, "shop-now-freezed", Util.getItemStackName(shop.getItem())).send();
        }
      } else {
        plugin.text().of(sender, "not-managed-shop").send();
      }
      return;
    }
    plugin.text().of(sender, "not-looking-at-shop").send();
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return Collections.emptyList();
  }
}
