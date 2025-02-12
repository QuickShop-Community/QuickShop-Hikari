package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopDisplayEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SubCommand_ToggleDisplay implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_ToggleDisplay(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Shop shop = getLookingShop(sender);
    if(shop != null) {
      if(shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.TOGGLE_DISPLAY)
         || plugin.perm().hasPermission(sender, "quickshop.other.toggledisplay")) {

        ShopDisplayEvent event = (ShopDisplayEvent)ShopDisplayEvent.PRE(shop, shop.isDisableDisplay(), !shop.isDisableDisplay());
        event.callEvent();

        event = (ShopDisplayEvent)event.clone(Phase.MAIN);
        if(event.callCancellableEvent()) {

          plugin.text().of(sender, "plugin-cancelled", event.getCancelReason());
          return;
        }

        if(event.updated()) {
          shop.setDisableDisplay(false);

          plugin.text().of(sender, "display-turn-on").send();
        } else {
          shop.setDisableDisplay(true);

          plugin.text().of(sender, "display-turn-off").send();
        }

        event = (ShopDisplayEvent)event.clone(Phase.POST);
        event.callEvent();
      } else {
        plugin.text().of(sender, "not-managed-shop").send();
      }
    } else {
      plugin.text().of(sender, "not-looking-at-shop").send();
    }
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return Collections.emptyList();
  }

}
