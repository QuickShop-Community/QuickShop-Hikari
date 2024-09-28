package com.ghostchu.quickshop.menu.staff;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import net.kyori.adventure.text.Component;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.shared.QuickShopPage.get;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getList;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getShop;

/**
 * PlayerSelectionMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class PlayerSelectionPage {

  protected final String returnMenu;
  protected final String menuName;
  protected final int menuPage;
  protected final int returnPage;
  protected final String playerPageID;
  protected final int menuRows;
  protected final String iconLore;
  protected final IconAction[] actions;

  public PlayerSelectionPage(final String returnMenu, final String menuName,
                             final int menuPage, final int returnPage, final String playerPageID,
                             final int menuRows, final String iconLore, final IconAction... actions) {

    this.returnMenu = returnMenu;
    this.menuName = menuName;
    this.menuPage = menuPage;
    this.returnPage = returnPage;
    this.playerPageID = playerPageID;
    this.iconLore = iconLore;
    this.actions = actions;

    //we need a controller row and then at least one row for items.
    this.menuRows = (menuRows <= 1)? 2 : menuRows;
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewer = callback.getPlayer().viewer();
    if(viewer.isPresent()) {

      final Optional<Shop> shop = getShop(viewer.get());
      if(shop.isPresent()) {

        final List<OfflinePlayer> players = sorted(shop.get());

        callback.getPage().getIcons().clear();
        final UUID id = viewer.get().uuid();
        final int offset = 9;
        final int page = (Integer)viewer.get().dataOrDefault(playerPageID, 1);
        final int items = (menuRows - 1) * offset;
        final int start = ((page - 1) * offset);

        final int maxPages = (players.size() / items) + (((players.size() % items) > 0)? 1 : 0);

        final int prev = (page <= 1)? maxPages : page - 1;
        final int next = (page >= maxPages)? 1 : page + 1;

        if(maxPages > 1) {

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("RED_WOOL", 1)
                                                             .display(get(id, "gui.shared.previous-page")))
                                             .withActions(new DataAction(playerPageID, prev), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(0)
                                             .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("GREEN_WOOL", 1)
                                                             .display(get(id, "gui.shared.next-page")))
                                             .withActions(new DataAction(playerPageID, next), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(8)
                                             .build());
        }

        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("BARRIER", 1)
                                                           .display(get(id, "gui.shared.previous-menu")))
                                           .withActions(new SwitchPageAction(returnMenu, returnPage))
                                           .withSlot(4)
                                           .build());

        int i = 0;
        for(final OfflinePlayer player : players) {

          final UUID uuid = player.getUniqueId();
          if(i < start) {

            i++;

            continue;
          }
          if(i >= (start + items)) break;

          SkullProfile profile = null;
          try {

            if(player.hasPlayedBefore()) {
              profile = new SkullProfile();

              profile.setUuid(uuid);
            }

          } catch(final Exception ignore) { }

          final String name = (player.getName() != null)? player.getName() : uuid.toString();
          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                             .display(Component.text(name))
                                                             .lore(getList(id, iconLore))
                                                             .profile(profile))
                                             .withActions(actions)
                                             .withActions(new RunnableAction((click)->{
                                               shop.get().setPlayerGroup(uuid, BuiltInShopPermissionGroup.STAFF);
                                               QuickShop.getInstance().text().of(id, "shop-staff-added", name).send();
                                             }), new SwitchPageAction(returnMenu, returnPage))
                                             .withSlot(offset + (i - start))
                                             .build());

          i++;
        }
      }
    }
  }

  public List<OfflinePlayer> sorted(final Shop shop) {

    final List<OfflinePlayer> sortedPlayers = new ArrayList<>();

    final List<UUID> staffs = shop.playersCanAuthorize(BuiltInShopPermissionGroup.STAFF);

    for(final OfflinePlayer player : Bukkit.getOfflinePlayers()) {

      final UUID id = player.getUniqueId();
      if(id.equals(shop.getOwner().getUniqueId()) || staffs.contains(id)) {
        continue;
      }
      sortedPlayers.add(player);
    }
    return sortedPlayers;
  }
}