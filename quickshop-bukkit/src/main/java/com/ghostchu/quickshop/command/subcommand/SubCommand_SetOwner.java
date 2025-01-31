package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopOwnerEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.util.Util.getPlayerList;

public class SubCommand_SetOwner implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_SetOwner(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.no-owner-given").send();
      return;
    }
    final Shop shop = getLookingShop(sender);

    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }

    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.OWNERSHIP_TRANSFER)
       && !plugin.perm().hasPermission(sender, "quickshop.other.setowner")) {
      plugin.text().of(sender, "no-permission").send();
      return;
    }
    QUserImpl.createAsync(plugin.getPlayerFinder(), parser.getArgs().get(0))
            .thenAccept(newShopOwner->{
              if(newShopOwner == null) {
                plugin.text().of(sender, "unknown-player").send();
                return;
              }

              ShopOwnerEvent event = new ShopOwnerEvent(Phase.PRE, shop, shop.getOwner(), newShopOwner);
              event.callEvent();

              final ShopOwnerEvent main = (ShopOwnerEvent)event.clone(Phase.MAIN);
              if(main.callCancellableEvent()) {
                return;
              }

              Util.mainThreadRun(()->{
                shop.setOwner(main.updated());

                plugin.text().of(sender, "command.new-owner", main.updated().getDisplay()).send();
              });

              event = (ShopOwnerEvent)main.clone(Phase.POST);
              event.callEvent();

            })
            .exceptionally(throwable->{
              plugin.text().of(sender, "internal-error", throwable.getMessage()).send();
              return null;
            });

  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return parser.getArgs().size() <= 1? getPlayerList() : Collections.emptyList();
  }

}
