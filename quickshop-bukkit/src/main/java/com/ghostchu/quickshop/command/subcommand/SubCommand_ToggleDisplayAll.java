package com.ghostchu.quickshop.command.subcommand;
/*
 * IslandSurvival
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.settings.type.ShopDisplayEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ToggleDisplayAll
 *
 * @author creatorfromhell
 * @since 6.2.0.10
 */
public class SubCommand_ToggleDisplayAll implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_ToggleDisplayAll(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    //should we be toggling displays off?
    boolean off = true;
    if(!parser.getArgs().isEmpty()) {
      off = (!parser.getArgs().getFirst().equalsIgnoreCase("on"));
    }

    boolean server = false;
    if(parser.getArgs().size() >= 2) {
      server = parser.getArgs().get(1).equalsIgnoreCase("server");
    }

    final boolean serverFinal = server;

    final QUser senderQUser = QUserImpl.createFullFilled(sender);
    final List<Shop> shopsToToggle = shopsToToggle(sender, senderQUser, serverFinal);

    for(final Shop shop : shopsToToggle) {

      if(shop.isDisableDisplay() == off) continue;

      if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.TOGGLE_DISPLAY)
         && !plugin.perm().hasPermission(sender, "quickshop.other.toggledisplay")) {
        plugin.text().of(sender, "not-managed-shop").send();

        return;
      }

      ShopDisplayEvent event = new ShopDisplayEvent(Phase.PRE, shop, shop.isDisableDisplay(), !shop.isDisableDisplay());
      event.callEvent();

      event = event.clone(Phase.MAIN);
      if(event.callCancellableEvent()) {

        plugin.text().of(sender, "plugin-cancelled", event.getCancelReason());
        return;
      }

      shop.setDisableDisplay(event.updated());

      event = event.clone(Phase.POST);
      event.callEvent();
    }

    final String message = (server)? ((off)? "display-turn-off-all" : "display-turn-on-all") : ((off)? "display-turn-off-owned" : "display-turn-on-owned");
    plugin.text().of(sender, message).send();
  }

  private List<Shop> shopsToToggle(final Player sender, final QUser senderQUser, final boolean server) {

    if(sender.hasPermission("quickshop.toggledisplayall.admin") && server) {
      return plugin.getShopManager().getAllShops();
    }
    return plugin.getShopManager().getAllShops(senderQUser);
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 2) {
      final List<String> completion = new ArrayList<>();
      completion.add("owned");

      if(sender.hasPermission("quickshop.toggledisplayall.admin")) {
        completion.add("server");
      }
      return completion;
    }

    if(parser.getArgs().size() == 1) {
      return Arrays.asList("off", "on");
    }
    return Collections.emptyList();
  }
}