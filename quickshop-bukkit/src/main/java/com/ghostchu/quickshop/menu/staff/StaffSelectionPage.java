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
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.menu.shared.ClearSearchAction;
import com.ghostchu.quickshop.menu.shared.GuiChatAction;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.Util;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.SHOP_DATA_ID;
import static com.ghostchu.quickshop.menu.ShopStaffMenu.STAFF_ADD;
import static com.ghostchu.quickshop.menu.ShopStaffMenu.STAFF_SEARCH;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getShop;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.guiMessage;

/**
 * StaffSelectionPage
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

        final List<UUID> allStaffs = shop.get().playersCanAuthorize(BuiltInShopPermissionGroup.STAFF);

        callback.getPage().getIcons().clear();
        callback.getPage().setLockEmptySlots(true);
        final UUID id = viewer.get().uuid();
        final Player viewerPlayer = Bukkit.getPlayer(id);
        if(viewerPlayer != null) {

          // Load GUI configuration for modern styling
          final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("staff");
          final GuiConfig.IconConfig borderConfig = menuConfig != null? menuConfig.getIcon("border") : null;
          final GuiConfig.IconConfig prevPageConfig = menuConfig != null? menuConfig.getIcon("previous-page") : null;
          final GuiConfig.IconConfig nextPageConfig = menuConfig != null? menuConfig.getIcon("next-page") : null;
          final GuiConfig.IconConfig pageInfoConfig = menuConfig != null? menuConfig.getIcon("page-info") : null;
          final GuiConfig.IconConfig addStaffConfig = menuConfig != null? menuConfig.getIcon("add-staff") : null;
          final GuiConfig.IconConfig backConfig = menuConfig != null? menuConfig.getIcon("back") : null;
          final GuiConfig.IconConfig searchConfig = menuConfig != null? menuConfig.getIcon("search") : null;

          // Get search query from viewer data
          final String searchQuery = (String)viewer.get().dataOrDefault(STAFF_SEARCH, "");

          // Filter staffs by search query
          final List<UUID> staffs = filterStaffs(allStaffs, searchQuery);

          // Set up borders from config (rows 1 and 6 like browse page)
          final String borderMaterial = borderConfig != null? borderConfig.getMaterial() : "GRAY_STAINED_GLASS_PANE";
          final IconBuilder borderBuilder = new IconBuilder(QuickShop.getInstance().stack().of(borderMaterial, 1));
          final List<Integer> borderRows = borderConfig != null? borderConfig.getRows() : List.of(1, 6);
          for(final int row : borderRows) {
            callback.getPage().setRow(row, borderBuilder);
          }

          // Get list start slot from config (slot 9 = row 2 like browse page)
          final int listStartSlot = menuConfig != null? menuConfig.getSection().getInt("list-start-slot", 9) : 9;

          final int offset = 9;
          final int page = (Integer)viewer.get().dataOrDefault(staffPageID, 1);
          final int items = (menuRows - 2) * offset; // Adjusted for border rows
          final int start = ((page - 1) * offset);

          final int maxPages = (staffs.size() / items) + (((staffs.size() % items) > 0)? 1 : 0);

          final int prev = (page <= 1)? maxPages : page - 1;
          final int next = (page >= maxPages)? 1 : page + 1;

          // === Control Row (Row 1) ===

          // Search button (slot 0) - Left-click to search, Right-click to clear
          final String searchMaterial = searchConfig != null? searchConfig.getMaterial() : "ANVIL";
          final int searchSlot = searchConfig != null? searchConfig.getSlot() : 0;
          final String currentSearchDisplay = searchQuery.isEmpty()? "None" : searchQuery;

          // Capture variables for closure
          final Long capturedShopId = shop.get().getShopId();

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(searchMaterial, 1)
                                                             .customName(getConfigDisplay(id, searchConfig, "<yellow>Search: {0}</yellow>", currentSearchDisplay))
                                                             .lore(getConfigLore(id, searchConfig, currentSearchDisplay)))
                                             .withSlot(searchSlot)
                                             .withActions(new GuiChatAction((message)->{
                                               // Handle clear command
                                               final String searchValue = (message.equalsIgnoreCase("clear") || message.equals("0"))? "" : message;

                                               // Create new viewer with state preserved + new search value
                                               final net.tnemc.menu.core.viewer.MenuViewer newViewer = new net.tnemc.menu.core.viewer.MenuViewer(id);
                                               newViewer.addData(SHOP_DATA_ID, capturedShopId);  // Use shop ID like other menus
                                               newViewer.addData(STAFF_SEARCH, searchValue);
                                               newViewer.addData(staffPageID, 1); // Reset to page 1 on new search
                                               net.tnemc.menu.core.manager.MenuManager.instance().addViewer(newViewer);

                                               // Reopen the menu
                                               final Player p = Bukkit.getPlayer(id);
                                               if(p != null && p.isOnline()) {
                                                 final net.tnemc.menu.core.compatibility.MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(p);
                                                 menuPlayer.inventory().openMenu(menuPlayer, menuName, menuPage);
                                               }
                                               return true;
                                             }, guiMessage("staff.enter-search"), false, ActionType.LEFT_CLICK))  // Left-click for search input
                                             .withActions(new ClearSearchAction(STAFF_SEARCH, staffPageID, menuName, menuPage))  // Right-click to clear
                                             .build());

          // Add staff button (slot 4 - center)
          final String addStaffMaterial = addStaffConfig != null? addStaffConfig.getMaterial() : "EMERALD";
          final int addStaffSlot = addStaffConfig != null? addStaffConfig.getSlot() : 4;
          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(addStaffMaterial, 1)
                                                             .customName(getConfigDisplay(id, addStaffConfig, "<green>Add Staff Member</green>"))
                                                             .lore(getConfigLore(id, addStaffConfig)))
                                             .withActions(new SwitchPageAction(menuName, STAFF_ADD))
                                             .withSlot(addStaffSlot)
                                             .build());

          // Back button (slot 8 - right side like browse close button)
          final String backMaterial = backConfig != null? backConfig.getMaterial() : "OAK_DOOR";
          final int backSlot = backConfig != null? backConfig.getSlot() : 8;
          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(backMaterial, 1)
                                                             .customName(getConfigDisplay(id, backConfig, "<white>Back to Shop</white>")))
                                             .withActions(new SwitchPageAction(returnMenu, returnPage))
                                             .withSlot(backSlot)
                                             .build());

          // === Pagination Row (Bottom - Row 6) ===
          final String prevMaterial = prevPageConfig != null? prevPageConfig.getMaterial() : "ARROW";
          final int prevSlot = prevPageConfig != null? prevPageConfig.getSlot() : 48;
          final String nextMaterial = nextPageConfig != null? nextPageConfig.getMaterial() : "ARROW";
          final int nextSlot = nextPageConfig != null? nextPageConfig.getSlot() : 50;
          final String pageInfoMaterial = pageInfoConfig != null? pageInfoConfig.getMaterial() : "BOOK";
          final int pageInfoSlot = pageInfoConfig != null? pageInfoConfig.getSlot() : 49;

          if(maxPages > 1) {
            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(prevMaterial, 1)
                                                               .customName(getConfigDisplay(id, prevPageConfig, "<white><< Previous Page</white>")))
                                               .withActions(new DataAction(staffPageID, prev), new SwitchPageAction(menuName, menuPage))
                                               .withSlot(prevSlot)
                                               .build());

            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextMaterial, 1)
                                                               .customName(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>")))
                                               .withActions(new DataAction(staffPageID, next), new SwitchPageAction(menuName, menuPage))
                                               .withSlot(nextSlot)
                                               .build());
          }

          // Page info (always show)
          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                                             .customName(getConfigDisplay(id, pageInfoConfig, "<yellow>Page {0}/{1}</yellow>", page, Math.max(1, maxPages))))
                                             .withSlot(pageInfoSlot)
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

                profile.uuid(uuid);
              }

            } catch(final Exception ignore) { }

            final String name = (player.isPresent() && player.get().getName() != null)? player.get().getName() : uuid.toString();
            callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                               .customName(QuickShop.getInstance().platform().miniMessage().deserialize("<yellow>" + name + "</yellow>"))
                                                               .lore(getConfigLore(id, null, name))
                                                               .profile(profile))
                                               .withActions(new GuiChatAction((message)->{
                                                 if(!message.isEmpty()) {
                                                   if(message.equalsIgnoreCase("confirm")) {
                                                     shop.get().setPlayerGroup(uuid, BuiltInShopPermissionGroup.EVERYONE);
                                                     QuickShop.getInstance().text().of(id, "shop-staff-deleted", name).send();
                                                     return true;
                                                   }
                                                   return true;
                                                 }
                                                 viewerPlayer.sendMessage(guiMessage("staff.confirm-remove", name));
                                                 return false;
                                               }, guiMessage("staff.confirm-remove", name), true, ActionType.LEFT_CLICK))  // Reopen to refresh staff list
                                               .withActions(new GuiChatAction((message)->{
                                                 if(!message.isEmpty()) {
                                                   if(message.equalsIgnoreCase("confirm")) {
                                                     if(shop.get().playerAuthorize(id, BuiltInShopPermission.OWNERSHIP_TRANSFER)) {
                                                       Util.regionThread(shop.get().bukkitLocation(), ()->ShopUtil.transferRequest(id, uuid, name, shop.get()));
                                                     } else {
                                                       QuickShop.getInstance().text().of(id, "no-permission").send();
                                                     }
                                                     return true;
                                                   }
                                                   return true;
                                                 }
                                                 viewerPlayer.sendMessage(guiMessage("staff.confirm-transfer", name));
                                                 return false;
                                               }, guiMessage("staff.confirm-transfer", name), false, ActionType.RIGHT_CLICK))  // Don't reopen after transfer
                                               .withSlot(listStartSlot + (i - start))
                                               .build());

            i++;
          }
        }
      }
    }
  }

  /**
   * Filter staff UUIDs by search query (player name)
   *
   * @param staffs      List of staff UUIDs
   * @param searchQuery Search query to filter by
   *
   * @return Filtered list of staff UUIDs
   */
  private List<UUID> filterStaffs(final List<UUID> staffs, final String searchQuery) {

    if(searchQuery == null || searchQuery.trim().isEmpty()) {
      return staffs;
    }

    final String query = searchQuery.toLowerCase(Locale.ROOT).trim();

    return staffs.stream()
            .filter(uuid->{
              final Optional<OfflinePlayer> player = QuickShopPage.getPlayer(uuid);
              if(player.isPresent() && player.get().getName() != null) {
                return player.get().getName().toLowerCase(Locale.ROOT).contains(query);
              }
              // Also match UUID if name is not available
              return uuid.toString().toLowerCase(Locale.ROOT).contains(query);
            })
            .toList();
  }
}