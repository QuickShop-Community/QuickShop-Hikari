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
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.menu.shared.ClearSearchAction;
import com.ghostchu.quickshop.menu.shared.GuiChatAction;
import com.ghostchu.quickshop.api.inventory.SkullProvider;
import net.tnemc.item.providers.SkullProfile;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.icon.action.impl.DataAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.icon.action.impl.SwitchPageAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.ghostchu.quickshop.menu.ShopKeeperMenu.SHOP_DATA_ID;
import static com.ghostchu.quickshop.menu.ShopStaffMenu.PLAYER_SEARCH;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigDisplay;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getConfigLore;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.getShop;
import static com.ghostchu.quickshop.menu.shared.QuickShopPage.guiMessage;

/**
 * PlayerSelectionPage
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
  protected final SkullProvider skullProvider;
  protected final IconAction[] actions;

  public PlayerSelectionPage(final String returnMenu, final String menuName,
                             final int menuPage, final int returnPage, final String playerPageID,
                             final int menuRows, final String iconLore, final SkullProvider skullProvider,
                             final IconAction... actions) {

    this.returnMenu = returnMenu;
    this.menuName = menuName;
    this.menuPage = menuPage;
    this.returnPage = returnPage;
    this.playerPageID = playerPageID;
    this.iconLore = iconLore;
    this.skullProvider = skullProvider;
    this.actions = actions;

    //we need a controller row and then at least one row for items.
    this.menuRows = (menuRows <= 1)? 2 : menuRows;
  }

  public void handle(final PageOpenCallback callback) {

    final Optional<MenuViewer> viewer = callback.getPlayer().viewer();
    if(viewer.isPresent()) {

      final Optional<Shop> shop = getShop(viewer.get());
      if(shop.isPresent()) {

        final List<OfflinePlayer> allPlayers = sorted(shop.get());

        callback.getPage().getIcons().clear();
        callback.getPage().setLockEmptySlots(true);
        final UUID id = viewer.get().uuid();

        // Load GUI configuration for modern styling
        final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("staff");
        final GuiConfig.IconConfig borderConfig = menuConfig != null? menuConfig.getIcon("border") : null;
        final GuiConfig.IconConfig prevPageConfig = menuConfig != null? menuConfig.getIcon("previous-page") : null;
        final GuiConfig.IconConfig nextPageConfig = menuConfig != null? menuConfig.getIcon("next-page") : null;
        final GuiConfig.IconConfig pageInfoConfig = menuConfig != null? menuConfig.getIcon("page-info") : null;
        final GuiConfig.IconConfig backConfig = menuConfig != null? menuConfig.getIcon("back") : null;
        final GuiConfig.IconConfig searchConfig = menuConfig != null? menuConfig.getIcon("search") : null;

        // Get search query from viewer data
        final String searchQuery = (String)viewer.get().dataOrDefault(PLAYER_SEARCH, "");

        // Filter players by search query
        final List<OfflinePlayer> players = filterPlayers(allPlayers, searchQuery);

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
        final int page = (Integer)viewer.get().dataOrDefault(playerPageID, 1);
        final int items = (menuRows - 2) * offset; // Adjusted for border rows
        final int start = ((page - 1) * offset);

        final int maxPages = (players.size() / items) + (((players.size() % items) > 0)? 1 : 0);

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
                                                           .customName(getConfigDisplay(id, searchConfig, "name", "<yellow>Search: {0}</yellow>", currentSearchDisplay))
                                                           .lore(getConfigLore(id, searchConfig, currentSearchDisplay)))
                                           .withSlot(searchSlot)
                                           .withActions(new GuiChatAction((message)->{
                                             // Handle clear command
                                             final String searchValue = (message.equalsIgnoreCase("clear") || message.equals("0"))? "" : message;

                                             // Create new viewer with state preserved + new search value
                                             final net.tnemc.menu.core.viewer.MenuViewer newViewer = new net.tnemc.menu.core.viewer.MenuViewer(id);
                                             newViewer.addData(SHOP_DATA_ID, capturedShopId);  // Use shop ID like other menus
                                             newViewer.addData(PLAYER_SEARCH, searchValue);
                                             newViewer.addData(playerPageID, 1); // Reset to page 1 on new search
                                             net.tnemc.menu.core.manager.MenuManager.instance().addViewer(newViewer);

                                             // Reopen the menu at the add staff page
                                             final Player p = Bukkit.getPlayer(id);
                                             if(p != null && p.isOnline()) {
                                               final net.tnemc.menu.core.compatibility.MenuPlayer menuPlayerObj = QuickShop.getInstance().createMenuPlayer(p);
                                               menuPlayerObj.inventory().openMenu(menuPlayerObj, menuName, menuPage);
                                             }
                                             return true;
                                           }, guiMessage("staff.enter-search"), false, ActionType.LEFT_CLICK))  // Left-click for search input
                                           .withActions(new ClearSearchAction(PLAYER_SEARCH, playerPageID, menuName, menuPage))  // Right-click to clear
                                           .build());

        // Back button (slot 8 - right side like browse close button)
        final String backMaterial = backConfig != null? backConfig.getMaterial() : "OAK_DOOR";
        final int backSlot = backConfig != null? backConfig.getSlot() : 8;
        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(backMaterial, 1)
                                                           .customName(getConfigDisplay(id, backConfig, "<white>Back to Staff List</white>")))
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
                                             .withActions(new DataAction(playerPageID, prev), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(prevSlot)
                                             .build());

          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(nextMaterial, 1)
                                                             .customName(getConfigDisplay(id, nextPageConfig, "<white>Next Page >></white>")))
                                             .withActions(new DataAction(playerPageID, next), new SwitchPageAction(menuName, menuPage))
                                             .withSlot(nextSlot)
                                             .build());
        }

        // Page info (always show)
        callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of(pageInfoMaterial, 1)
                                                           .customName(getConfigDisplay(id, pageInfoConfig, "<yellow>Page {0}/{1}</yellow>", page, Math.max(1, maxPages))))
                                           .withSlot(pageInfoSlot)
                                           .build());

        int i = 0;
        for(final OfflinePlayer player : players) {

          final UUID uuid = player.getUniqueId();
          if(i < start) {

            i++;

            continue;
          }
          if(i >= (start + items)) break;

          final SkullProfile profile = getOrLoadProfile(uuid);

          final String name = (player.getName() != null)? player.getName() : uuid.toString();
          callback.getPage().addIcon(new IconBuilder(QuickShop.getInstance().stack().of("PLAYER_HEAD", 1)
                                                             .customName(QuickShop.getInstance().platform().miniMessage().deserialize("<yellow>" + name + "</yellow>"))
                                                             .lore(getConfigLore(id, null, name))
                                                             .profile(profile))
                                             .withActions(actions)
                                             .withActions(new RunnableAction((click)->{
                                               shop.get().setPlayerGroup(uuid, BuiltInShopPermissionGroup.STAFF);
                                               QuickShop.getInstance().text().of(id, "shop-staff-added", name).send();
                                             }), new SwitchPageAction(returnMenu, returnPage))
                                             .withSlot(listStartSlot + (i - start))
                                             .build());

          i++;
        }
      }
    }
  }

  private SkullProfile getOrLoadProfile(final UUID uuid) {

    final SkullProfile cached = skullProvider.getCachedProfile(uuid);
    if(cached != null) {
      return cached;
    }

    skullProvider.provideProfile(uuid);

    final SkullProfile fallback = new SkullProfile();
    fallback.uuid(uuid);
    return fallback;
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

  /**
   * Filter players by search query (player name)
   *
   * @param players     List of players to filter
   * @param searchQuery Search query to filter by
   *
   * @return Filtered list of players
   */
  private List<OfflinePlayer> filterPlayers(final List<OfflinePlayer> players, final String searchQuery) {

    if(searchQuery == null || searchQuery.trim().isEmpty()) {
      return players;
    }

    final String query = searchQuery.toLowerCase(Locale.ROOT).trim();

    return players.stream()
            .filter(player->{
              try {
                if(player.getName() != null) {
                  return player.getName().toLowerCase(Locale.ROOT).contains(query);
                }
              } catch (final RuntimeException | LinkageError ex) {
                QuickShop.getInstance().logger().warn("Unable to read offline player "
                                                      + player.getUniqueId()
                                                      + " while building the staff selection menu. This is related to https://github.com/PaperMC/Paper/issues/13312");
                return false;
              }
              // Also match UUID if name is not available
              return player.getUniqueId().toString().toLowerCase(Locale.ROOT).contains(query);
            })
            .toList();
  }
}