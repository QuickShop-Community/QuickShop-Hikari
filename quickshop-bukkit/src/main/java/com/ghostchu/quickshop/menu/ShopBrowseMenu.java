package com.ghostchu.quickshop.menu;
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
import com.ghostchu.quickshop.menu.browse.GroupedItemPage;
import com.ghostchu.quickshop.menu.browse.ShopListPage;
import com.ghostchu.quickshop.config.GuiConfig;
import net.tnemc.menu.core.Menu;
import net.tnemc.menu.core.Page;

/**
 * ShopBrowseMenu - Enhanced market browser with grouping, filtering, sorting, and search
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopBrowseMenu extends Menu {

  // Data keys for viewer state
  public static final String SHOPS_DATA = "BROWSE_SHOPS";
  public static final String SHOPS_PAGE = "BROWSE_PAGE";
  public static final String BROWSE_SORT = "BROWSE_SORT";
  public static final String BROWSE_FILTER = "BROWSE_FILTER";
  public static final String BROWSE_SEARCH = "BROWSE_SEARCH";
  public static final String BROWSE_STOCK_ONLY = "BROWSE_STOCK_ONLY";
  public static final String BROWSE_WORLD_ONLY = "BROWSE_WORLD_ONLY";

  // Data keys for shop list page (when viewing shops for a specific item)
  public static final String SELECTED_ITEM_SHOPS = "SELECTED_ITEM_SHOPS";
  public static final String SHOP_LIST_PAGE = "SHOP_LIST_PAGE";

  public ShopBrowseMenu() {

    // Load rows from GUI config
    final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("browse");
    this.rows = (menuConfig != null? menuConfig.getRows() : 6);
    this.name = "qs:browse";

    setOpen((open)->open.getMenu().setTitle(QuickShop.getInstance().text().of(open.getPlayer().identifier(), "gui.browse.title").legacy()));

    // Page 1: Grouped item view (market overview)
    final Page groupedPage = new Page(1);
    final GroupedItemPage groupedPageHandler = new GroupedItemPage(this.name, this.rows);
    groupedPage.setOpen(groupedPageHandler::handle);
    addPage(groupedPage);

    // Page 2: Shop list view (all shops for a specific item)
    final Page shopListPage = new Page(2);
    final ShopListPage shopListPageHandler = new ShopListPage(this.name, this.rows);
    shopListPage.setOpen(shopListPageHandler::handle);
    addPage(shopListPage);
  }
}