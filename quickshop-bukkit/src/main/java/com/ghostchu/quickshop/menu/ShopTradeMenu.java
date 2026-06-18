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
import com.ghostchu.quickshop.config.GuiConfig;
import com.ghostchu.quickshop.menu.shared.QuickShopMenu;
import com.ghostchu.quickshop.menu.trade.MainPage;

/**
 * ShopTradeMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopTradeMenu extends QuickShopMenu {

  public ShopTradeMenu() {

    // Load rows from config or use default (6 rows for modern layout)
    final GuiConfig.MenuConfig menuConfig = QuickShop.getInstance().getGuiConfig().getMenuConfig("trade");
    this.rows = (menuConfig != null? menuConfig.getRows() : 6);
    this.name = "qs:trade";
    this.title = "Shop Trade";

    setOpen((open)->open.getMenu().setTitle(QuickShopMenu.getTitle(open.getPlayer().identifier(), menuConfig, this.title, null)));

    addPage(new MainPage());
  }
}