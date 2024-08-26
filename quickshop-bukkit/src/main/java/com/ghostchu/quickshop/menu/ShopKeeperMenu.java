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

import com.ghostchu.quickshop.menu.keeper.MainPage;
import com.ghostchu.quickshop.menu.shared.QuickShopMenu;

/**
 * ShopControlMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopKeeperMenu extends QuickShopMenu {

  public static final int KEEPER_MAIN = 1;

  public static final String SHOP_DATA_ID = "SHOP_ID";

  public ShopKeeperMenu() {

    this.rows = 3;
    this.name = "qs:keeper";

    setOpen((open)->open.getMenu().setTitle(legacy(open.getPlayer().identifier(), "gui.keeper.title")));

    addPage(new MainPage());
  }
}