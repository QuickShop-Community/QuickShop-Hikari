package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


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

    if(shop.getShopType().equals(ShopType.FROZEN)) {

      shop.setShopType(ShopType.BUYING);
      plugin.text().of(sender, "shop-nolonger-freezed", Util.getItemStackName(shop.getItem())).send();
      plugin.text().of(sender, "command.now-buying", Util.getItemStackName(shop.getItem())).send();
    } else {


      shop.setShopType(ShopType.FROZEN);
      plugin.text().of(sender, "shop-now-freezed", Util.getItemStackName(shop.getItem())).send();
    }
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    MsgUtil.sendControlPanelInfo(sender, shop);
  }
}
