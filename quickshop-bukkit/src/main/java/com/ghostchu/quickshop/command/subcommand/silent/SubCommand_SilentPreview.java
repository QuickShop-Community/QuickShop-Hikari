package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SubCommand_SilentPreview extends SubCommand_SilentBase {

  public SubCommand_SilentPreview(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    if(shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.PREVIEW_SHOP)
       || plugin.perm().hasPermission(sender, "quickshop.other.preview")) {
      shop.openPreview(sender);
    }
  }

}
