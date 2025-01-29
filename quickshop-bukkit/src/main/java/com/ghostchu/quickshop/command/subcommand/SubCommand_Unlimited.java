package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopUnlimitedEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.SimpleShopManager;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Unlimited implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Unlimited(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    final boolean newStatus = !shop.isUnlimited();

    ShopUnlimitedEvent event = new ShopUnlimitedEvent(Phase.PRE, shop, shop.isUnlimited(), newStatus);
    event.callEvent();

    event = (ShopUnlimitedEvent)event.clone(Phase.MAIN);
    if(event.callCancellableEvent()) {
      Log.debug("Other plugin cancelled shop naming.");
      return;
    }

    event = (ShopUnlimitedEvent)event.clone(Phase.POST);
    event.callEvent();

    shop.setUnlimited(event.updated());
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    if(shop.isUnlimited()) {
      plugin.text().of(sender, "command.toggle-unlimited.unlimited").send();
      if(plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
        final QUser qUser = ((SimpleShopManager)plugin.getShopManager()).getCacheUnlimitedShopAccount();
        plugin.getShopManager().migrateOwnerToUnlimitedShopOwner(shop);
        plugin.text().of(sender, "unlimited-shop-owner-changed", qUser).send();
      }
      return;
    }
    plugin.text().of(sender, "command.toggle-unlimited.limited").send();
    if(plugin.getConfig().getBoolean("unlimited-shop-owner-change")) {
      plugin.text().of(sender, "unlimited-shop-owner-keeped").send();
    }
  }

}