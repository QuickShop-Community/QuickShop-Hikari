package com.ghostchu.quickshop.command.subcommand;

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
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.ghostchu.quickshop.api.shop.tag.TagService.SYS_FAV;
import static com.ghostchu.quickshop.shop.tag.QuickShopTagService.MAX_TAG_LENGTH;

/**
 * SubCommand_Favorite
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SubCommand_Favorite implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Favorite(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender,
                        @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    final String tag = plugin.tagManager().service().normalizeTag(SYS_FAV, true);
    if(tag == null) {

      //should never happen, but we'll catch just in case.
      plugin.text().of(sender, "tags.general.invalid", MAX_TAG_LENGTH).send();
      return;
    }

    if(parser.getArgs().isEmpty()) {
      final Shop shop = getLookingShop(sender);
      if(shop == null) {
        plugin.text().of(sender, "not-looking-at-shop").send();
        return;
      }

      final TaggingResult result = plugin.tagManager().toggleTag(shop.getShopId(), sender.getUniqueId(), tag);

      switch(result) {
        case TaggingResult.SUCCESS -> {
          if(plugin.tagManager().hasTag(shop.getShopId(), sender.getUniqueId(), tag)) {
            plugin.text().of(sender, "tags.favorite.added").send();
          } else {
            plugin.text().of(sender, "tags.favorite.removed").send();
          }
        }
        case TaggingResult.DATABASE_ERROR ->
                plugin.text().of(sender, "tags.general.database-error").send();
        default -> plugin.text().of(sender, "tags.favorite.unable").send();
      }
      return;
    }

    final String sub = parser.getArgs().getFirst();
    switch(sub.toLowerCase(Locale.ROOT)) {
      case "list" -> {

        plugin.tagManager().listShopsByFilter(sender, new ArrayList<>(List.of(tag)),
                                                       "tags.favorite.list-title",
                                                       "tags.favorite.none");
      }
      default -> sendUsage(sender);
    }
  }

  @NotNull
  @Override
  public List<String> onTabComplete(@NotNull final Player sender,
                                    @NotNull final String commandLabel,
                                    @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("list");
    }
    return Collections.emptyList();
  }

  private void sendUsage(final Player sender) {

    plugin.text().of(sender, "command-incorrect", "/quickshop fav [list]").send();
  }
}