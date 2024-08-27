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

import com.ghostchu.quickshop.menu.trade.MainPage;
import com.ghostchu.quickshop.menu.shared.QuickShopMenu;

/**
 * ShopTradeMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopTradeMenu extends QuickShopMenu {

  public ShopTradeMenu() {

    this.rows = 5;
    this.name = "qs:trade";

    setOpen((open)->open.getMenu().setTitle(legacy(open.getPlayer().identifier(), "gui.trade.title")));

    addPage(new MainPage());
  }
}