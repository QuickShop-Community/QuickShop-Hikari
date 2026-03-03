package com.ghostchu.quickshop.addon.tags.command;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.ghostchu.quickshop.addon.tags.Util.SYS_AVOID;
import static com.ghostchu.quickshop.addon.tags.Util.SYS_WATCH;
import static com.ghostchu.quickshop.addon.tags.Util.handleTaggedList;

/**
 * SubCommand_Avoid
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SubCommand_Avoid implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Avoid(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      sendUsage(sender);
      return;
    }

    if(parser.getArgs().size() == 0) {

      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      //TODO: Toggle avoid for the shop
      return;
    }

    final String sub = parser.getArgs().getFirst();
    switch(sub.toLowerCase(Locale.ROOT)) {

      case "list": handleTaggedList(sender, new ArrayDeque<>(List.of(SYS_AVOID)));
    }
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("list");
    }
    return Collections.emptyList();
  }

  private void sendUsage(final Player sender) {
    plugin.text().of(sender, "command-incorrect",
                     "/quickshop avoid [list]")
            .send();
  }
}