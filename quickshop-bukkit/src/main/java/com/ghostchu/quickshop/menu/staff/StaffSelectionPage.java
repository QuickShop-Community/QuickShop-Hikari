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
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.ChatAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopStaffMenu.STAFF_ADD;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.get;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getList;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getShop;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.legacy;

/**
 * PlayerSelectionMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class StaffSelectionPage {

  protected final String returnMenu;
  protected final String menuName;
  protected final int menuPage;
  protected final int returnPage;
  protected final String staffPageID;
  protected final int menuRows;
  protected final String iconLore;
  protected final IconAction[] actions;

  public StaffSelectionPage(final String returnMenu, final String menuName,
                            final int menuPage, final int returnPage, final String staffPageID,
                            final int menuRows, final String iconLore, final IconAction... actions) {

    this.returnMenu = returnMenu;
    this.menuName = menuName;
    this.menuPage = menuPage;
    this.returnPage = returnPage;
    this.staffPageID = staffPageID;
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

        final List<UUID> staffs = shop.get().playersCanAuthorize(BuiltInShopPermissionGroup.STAFF);

        callback.getPage().getIcons().clear();
        final UUID id = viewer.get().uuid();
        final Player viewerPlayer = Bukkit.getPlayer(id);
        if(viewerPlayer != null) {
          final int offset = 9;
          final int page = (Integer)viewer.get().dataOrDefault(staffPageID, 1);
          final int items = (menuRows - 1) * offset;
          final int start = ((page - 1) * offset);

          final int maxPages = (staffs.size() / items) + (((staffs.size() % items) > 0)? 1 : 0);

          final int prev = (page <= 1)? maxPages : page - 1;
          final int next = (page >= maxPages)? 1 : page + 1;

          if(maxPages > 1) {

            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("RED_WOOL", 1)
                                                               .display(get(id, "gui.shared.previous-page")))
                                               .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                                               .withSlot(0)
                                               .build());

            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("GREEN_WOOL", 1)
                                                               .display(get(id, "gui.shared.next-page")))
                                               .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                                               .withSlot(8)
                                               .build());
          }

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                             .display(get(id, "gui.staff.add-staff")))
                                             .withActions(new SwitchPageAction(menuName, STAFF_ADD))
                                             .withSlot(2)
                                             .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("BARRIER", 1)
                                                             .display(get(id, "gui.shared.previous-menu")))
                                             .withActions(new SwitchPageAction(returnMenu, returnPage))
                                             .withSlot(4)
                                             .build());

          int i = 0;
          for(final UUID uuid : staffs) {

            final Optional<OfflinePlayer> player = QuickShopPage.getPlayer(uuid);

            if(i < start) {

              i++;

              continue;
            }
            if(i >= (start + items)) break;

            SkullProfile profile = null;
            try {

              if(player.isPresent() && player.get().hasPlayedBefore()) {
                profile = new SkullProfile();

                profile.setUuid(uuid);
              }

            } catch(final Exception ignore) { }

            final String name = (player.isPresent() && player.get().getName() != null)? player.get().getName() : uuid.toString();
            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                               .display(get(id, "gui.staff.head-icon.display", name))
                                                               .lore(getList(id, iconLore))
                                                               .profile(profile))
                                               .withActions(new ChatAction((message->{

                                                 if(!message.getMessage().isEmpty()) {

                                                   if(message.getMessage().equalsIgnoreCase("confirm")) {

                                                     shop.get().setPlayerGroup(uuid, BuiltInShopPermissionGroup.EVERYONE);
                                                     QuickShop.getInstance().text().of(id, "shop-staff-deleted", name).send();
                                                     viewer.get().close(QuickShop.getInstance().createMenuPlayer(viewerPlayer));
                                                     return true;
                                                   }
                                                   return true;
                                                 }
                                                 message.getPlayer().message(legacy(id, "gui.staff.confirm-remove", name));
                                                 return false;
                                               }), ActionType.LEFT_CLICK), new RunnableAction((run)->run.player().message(legacy(id, "gui.staff.confirm-remove", name)), ActionType.LEFT_CLICK))
                                               .withActions(new ChatAction((message->{

                                                 if(!message.getMessage().isEmpty()) {

                                                   if(message.getMessage().equalsIgnoreCase("confirm")) {
                                                     if(shop.get().playerAuthorize(id, BuiltInShopPermission.OWNERSHIP_TRANSFER)) {

                                                       Util.mainThreadRun(()->ShopUtil.transferRequest(id, uuid, name, shop.get()));
                                                     } else {

                                                       QuickShop.getInstance().text().of(id, "no-permission").send();
                                                     }
                                                     viewer.get().close(QuickShop.getInstance().createMenuPlayer(viewerPlayer));
                                                     return true;
                                                   }
                                                   return true;
                                                 }
                                                 message.getPlayer().message(legacy(id, "gui.staff.confirm-transfer", name));
                                                 return false;
                                               }), ActionType.RIGHT_CLICK), new RunnableAction((run)->run.player().message(legacy(id, "gui.staff.confirm-transfer", name)), ActionType.RIGHT_CLICK))
                                               .withSlot(offset + (i - start))
                                               .build());

            i++;
          }
        }
      }
    }
  }
}