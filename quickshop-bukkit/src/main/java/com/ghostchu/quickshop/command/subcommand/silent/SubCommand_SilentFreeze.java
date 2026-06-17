package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.ghostchu.quickshop.shop.SimpleShopManager.ACTIVE_STATE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.BUYING_TYPE;
import static com.ghostchu.quickshop.shop.SimpleShopManager.FROZEN_STATE;


public class SubCommand_SilentFreeze extends SubCommand_SilentBase {

  public SubCommand_SilentFreeze(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE)
       && !plugin.perm().hasPermission(sender, "quickshop.create.admin")) {
      plugin.text().of(sender, "not-permission").send();
      return;
    }

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

    MsgUtil.sendControlPanelInfo(sender, shop);

    shop.setSignText(plugin.text().findRelativeLanguages(sender));
  }
}
