package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.ghostchu.quickshop.shop.SimpleShopManager.SELLING_TYPE;

public class SubCommand_SilentSell extends SubCommand_SilentBase {

  public SubCommand_SilentSell(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(!plugin.perm().hasPermission(sender, "quickshop.create.sell")) {
      plugin.text().of("no-permission").send();
      return;
    }
    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE)
       && !plugin.perm().hasPermission(sender, "quickshop.create.admin")) {
      plugin.text().of(sender, "not-managed-shop").send();
      return;
    }

    shop.shopType(SELLING_TYPE);
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    MsgUtil.sendControlPanelInfo(sender, shop);
    plugin.text().of(sender,
                     "command.now-selling", Util.getItemStackName(shop.getItem())).send();

  }
}