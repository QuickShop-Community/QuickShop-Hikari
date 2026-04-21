package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.ghostchu.quickshop.shop.SimpleShopManager.BUYING_TYPE;


public class SubCommand_SilentBuy extends SubCommand_SilentBase {

  public SubCommand_SilentBuy(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE)
       && !plugin.perm().hasPermission(sender, "quickshop.create.admin")) {
      plugin.text().of(sender, "not-permission").send();
      return;
    }

    shop.shopType(BUYING_TYPE);
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    MsgUtil.sendControlPanelInfo(sender, shop);
    plugin.text().of(sender, "command.now-buying", Util.getItemStackName(shop.getItem())).send();

    Util.playSound(sender, "effect.sound.shop.mode-change");
    Util.playParticle(sender, shop.bukkitLocation(), "effect.particle.shop.mode-change");
  }
}